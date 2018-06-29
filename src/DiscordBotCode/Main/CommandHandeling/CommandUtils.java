package DiscordBotCode.Main.CommandHandeling;

import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.Events.CommandInputEvents;
import DiscordBotCode.Main.CustomEvents.CommandExecutedEvent;
import DiscordBotCode.Main.CustomEvents.CommandFailedExecuteEvent;
import DiscordBotCode.Main.CustomEvents.CommandRegisterEvent;
import DiscordBotCode.Main.CustomEvents.CommandRemoveEvent;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Misc.Annotation.DataObject;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandUtils
{
	public static ConcurrentHashMap<String, DiscordCommand> discordChatCommands = new ConcurrentHashMap<>(); //All commands added to the program
	public static ConcurrentHashMap<String, CopyOnWriteArrayList<String>> commandCategories = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> commandCategory = new ConcurrentHashMap<>();
	
	@DataObject(file_path = "commandStates.json", name = "disabledCommands")
	public static ConcurrentHashMap<Long, ArrayList<String>> disabledCommands = new ConcurrentHashMap<>();
	
	public static CopyOnWriteArrayList<ICommandFormatter> formatters = new CopyOnWriteArrayList<>();
	
	public static void executeCommand( IMessage m1T )
	{
		IMessage m1 = formatMessage(m1T);
		String text = m1.getContent();
		
		DiscordCommand command = getDiscordCommand(text, m1.getChannel());
		
		if (command != null) {
			String[] args = getArgsFromText(text, command, m1.getChannel());
			boolean permission = command.hasPermissions(m1, args);
			
			if (!command.commandPrivateChat() && m1.getChannel().isPrivate()) {
				ChatUtils.sendMessage(m1.getChannel(), "*This command is not available for private channels!*");
				return;
			}
			
			if (permission) {
				if (command.canExecute(m1, args)) { //Execute the command and send it to the botBase
					try {
						command.commandExecuted(m1, args);
						DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandExecutedEvent(command, m1, Thread.currentThread()));
					}catch (Exception e){
						DiscordBotBase.handleException(e);
						DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandFailedExecuteEvent(command, m1, Thread.currentThread()));//DiscordCommand failed from unknown error. Sending it to botBase
					}
					
					return; //End code on successful command execution
				} else {
					ChatUtils.sendMessage(m1.getChannel(), "*Could not execute command: " + m1.getContent() + "*");
				}
			}else{
				if(PermissionUtils.getRequiredRole(command, m1.getChannel()) != null){
					if(!PermissionUtils.hasRole(m1.getAuthor(), m1.getGuild(), PermissionUtils.getRequiredRole(command, m1.getChannel()), true)){
						ChatUtils.sendMessage(m1.getChannel(), m1.getAuthor().mention() + " You must be ` " + PermissionUtils.getRequiredRole(command, m1.getChannel()).getName() + " ` or above to use this command!");
					}
				}else {
					ChatUtils.sendMessage(m1.getChannel(), m1.getAuthor().mention() + " You do not have the required permissions to use this command!");
				}
			}
		}
		
		DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandFailedExecuteEvent(command, m1, Thread.currentThread()));//DiscordCommand failed from unknown error. Sending it to botBase
	}
	
	public static MessageObject getCurrentHandledMessage(){
		if(Thread.currentThread() instanceof CommandInputEvents.CommandHandlingThread){
			MessageObject ob = ((CommandInputEvents.CommandHandlingThread)Thread.currentThread()).curMessage;
			
			if(ob != null){
				return ob;
			}
		}
		
		return null;
	}
	
	private static ConcurrentHashMap<String, Field> messageFields = new ConcurrentHashMap<>();
	
	public static Field getField(String field){
		if(messageFields.containsKey(field) && messageFields.get(field) != null){
			messageFields.get(field);
		}else{
			if(!setField(field)){
				setFieldAlt(field);
			}
		}
		
		return messageFields.containsKey(field) && messageFields.get(field) != null ? messageFields.get(field) : null;
	}
	
	public static boolean setField(String key){
		try {
			Field fiel = Message.class.getDeclaredField(key);
			
			if(fiel != null){
				if(Modifier.isProtected(fiel.getModifiers())) { //All fields that are changeable are protected, therefor private fields can be used to have unchangeable values
					fiel.setAccessible(true);
					messageFields.put(key, fiel);
					return true;
				}
			}
			
		} catch (NoSuchFieldException ignored) {}
		
		return false;
	}
	
	public static boolean setFieldAlt(String key){
		try {
			Field fiel = MessageObject.class.getDeclaredField(key);
			
			if(fiel != null){
				fiel.setAccessible(true);
				messageFields.put(key, fiel);
				return true;
			}
			
		} catch (NoSuchFieldException ignored) {}
		
		return false;
	}
	
	
	public static Object getMessageValue(IMessage message, String key){
		if(message instanceof Message){
			try {
				Field field = getField(key);
				if(field != null) {
					return field.get(message);
				}else{
					System.err.println("Field with key: \"" + key + "\" returned null!");
				}
			} catch (IllegalAccessException e) {
				DiscordBotBase.handleException(e);
			}
		}
		
		return null;
	}
	
	public static void setMessageValue(IMessage message, String key, Object value){
		if(message instanceof Message && key != null && value != null){
			try {
				Field field = getField(key);
				if(field != null){
					field.set(message, value);
				}else{
					System.err.println("Field with key: \"" + key + "\" returned null!");
				}
			} catch (IllegalAccessException e) {
				DiscordBotBase.handleException(e);
			}
		}
	}
	
	private static IMessage formatMessage(IMessage message){
		IMessage message1 = message;
		
		for(ICommandFormatter commandFormatter : formatters){
			message1 = commandFormatter.getFormattedMessage(message1);
		}
		
		return message1;
	}
	
	private static DiscordCommand getCommand( String text, IChannel chat, boolean checkSign, boolean checkEnabled )
	{
		if (chat == null || text == null) {
			return null;
		}
		
		for (DiscordCommand command : discordChatCommands.values()) {
			if(checkEnabled){
				if(!chat.isPrivate() && chat.getGuild() != null && isCommandDisabled(chat.getGuild(), command)) continue;
			}
			
			if(command instanceof DevCommandBase){ //Allows dev and non dev commands to use the same prefix without issues on help command
				if (compareCommandPrefix(text, command, chat, true)) {
					return command;
				}
			}else {
				if (compareCommandPrefix(text, command, chat, checkSign)) {
					return command;
				}
			}
		}
		return null;
	}
	
	private static DiscordCommand getSubCommandFromCommand( String text, DiscordCommand command, IChannel channel, boolean checkEnabled )
	{
		if (text == null || text.isEmpty() || command == null) {
			return null;
		}
		
		if (command.subCommands.size() > 0) {
			for (DiscordCommand subCommand : command.subCommands) {
				if (subCommand == null) {
					continue;
				}
				
				if(checkEnabled){
					if(!channel.isPrivate() && channel.getGuild() != null && isCommandDisabled(channel.getGuild(), command)) continue;
				}
				
				if(compareCommandPrefix(text, subCommand, channel, false)){
					return subCommand;
				}
			}
		}
		
		return null;
	}
	
	
	public static DiscordCommand getDiscordCommand( String text, IChannel channel, boolean checkSign, boolean checkEnabled){
		DiscordCommand commandReturn = getCommand(text, channel, checkSign, checkEnabled);
		text = text.toLowerCase();
		
		if(commandReturn == null) return null;
		
		if(text.startsWith(commandReturn.getCommandSign(channel))){
			text = text.substring(commandReturn.getCommandSign(channel).length());
		}
		
		for(String t : commandReturn.commandPrefixes()){
			if(text.startsWith(t.toLowerCase())){
				text = text.substring(t.length());
				break;
			}
		}
		
		while(text.startsWith(" ") && text.length() > 1){
			text = text.substring(1);
		}
		
		while (getSubCommandFromCommand(text, commandReturn, channel, checkEnabled) != null) {
			commandReturn = getSubCommandFromCommand(text, commandReturn, channel, checkEnabled);
			
			for(String t : commandReturn.commandPrefixes()){
				if(text.startsWith(t.toLowerCase())){
					text = text.substring(t.length());
					break;
				}
			}
			
			while(text.startsWith(" ") && text.length() > 0){
				text = text.substring(1);
			}
		}
		
		return commandReturn;
	}
	
	
	public static DiscordCommand getDiscordCommand( String text, IChannel channel, boolean checkEnabled){
		return getDiscordCommand(text, channel, true, checkEnabled);
	}
	
	public static DiscordCommand getDiscordCommand( String text, IChannel channel){
		return getDiscordCommand(text, channel, true);
	}
	
	
	public static String[] getArgsFromText( String text, DiscordCommand command, IChannel channel )
	{
		if(text.startsWith(command.getCommandSign(channel))){
			text = text.substring(command.getCommandSign(channel).length());
		}
		
		ArrayList<DiscordCommand> commands = new ArrayList<>();
		DiscordCommand command1 = command;
		
		commands.add(command1);
		
		while(command1.isSubCommand()){
			command1 = command1.baseCommand;
			commands.add(command1);
		}
		
		Collections.reverse(commands);
		
		top:
		for(DiscordCommand command2 : commands){
			String[] tg = text.split(" ");
			
			if(tg.length > 0){
				for(String tk : command2.commandPrefixes()){
					if(tg[0].equalsIgnoreCase(tk)){
						text = text.substring(tk.length());
						
						while (text.startsWith(" ")){
							text = text.substring(1);
						}
						
						continue top;
					}
				}
			}
		}
		
		String temp = text;
		
		while (temp.startsWith(" ")){
			temp = temp.substring(1);
		}
		
		if(temp.isEmpty()){
			return new String[0];
		}
		
		String[] result = temp.split(" ");
		ArrayList<String> list = new ArrayList<>(Arrays.asList(result));
		list.removeIf(String::isEmpty);
		return list.toArray(new String[list.size()]);
	}
	
	private static boolean compareCommandPrefix( String text, DiscordCommand command, IChannel channel, boolean checkSign){
		return getCommandPrefix(text, command, channel, checkSign) != null;
	}
	
	
	private static String getCommandPrefix( String text, DiscordCommand command, IChannel channel, boolean checkSign){
		String commandPrefix = command.getCommandSign(channel) == null ? DiscordBotBase.getCommandSign() : command.getCommandSign(channel);
		
		if(!checkSign){
			text = text.replace(commandPrefix, "");
			commandPrefix = "";
		}
		
		if(command.isSubCommand()){
			for(String prefix : command.baseCommand.commandPrefixes()){
				String tempText = text;
				if(!command.caseSensitive()){
					prefix = prefix.toLowerCase();
					tempText = tempText.toLowerCase();
				}
				
				if(tempText.toLowerCase().startsWith(commandPrefix + prefix) || tempText.equals(commandPrefix + prefix)){
					commandPrefix += prefix + " ";
					break;
				}
			}
		}
		
		for(String prefix : command.commandPrefixes()){
			String tempText = text;
			if(!command.caseSensitive()){
				prefix = prefix.toLowerCase();
				tempText = tempText.toLowerCase();
			}
			
			prefix = commandPrefix + prefix;
			
			if(text.length() > prefix.length() ){ //Hacky implementation
				prefix += " ";
			}
			
			if(tempText.startsWith(prefix) || tempText.equals(prefix)){
				return prefix;
			}
		}
		
		return null;
	}
	
	public static String getKeyFromCommand(DiscordCommand command){
		if(command == null) return null;
		
		if(command.isSubCommand()){
			for (Map.Entry<String, DiscordCommand> ent : discordChatCommands.entrySet()) {
				if (Objects.equals(command.baseCommand.getClass().getName(), ent.getValue().getClass().getName())) {
					return ent.getKey() + ":" + command.commandPrefix();
				}
			}
		}else {
			for (Map.Entry<String, DiscordCommand> ent : discordChatCommands.entrySet()) {
				if(ent.getValue() == null || ent.getValue().getClass() == null) continue;
				
				if(command.getClass() != null && ent.getValue().getClass() != null) {
					if (Objects.equals(command.getClass().getName(), ent.getValue().getClass().getName())) {
						return ent.getKey();
					}
				}
			}
		}
		
		return null;
	}
	
	public static String getCommandCategory( DiscordCommand command){
		String key = getKeyFromCommand(command);
		return commandCategory.getOrDefault(key, null);
	}
	
	public static ArrayList<DiscordCommand> getCommandsFromCategory(String cat){
		ArrayList<DiscordCommand> list = new ArrayList<>();
		ArrayList<String> keys = new ArrayList<>();
		
		if(commandCategories.containsKey(cat)) {
			keys.addAll(commandCategories.get(cat));
		}
		
		for(String t : keys){
			if(discordChatCommands.containsKey(t)){
				list.add(discordChatCommands.get(t));
			}
		}
		
		return list;
	}
	
	public static boolean isCommandDisabled( IGuild guild, DiscordCommand base){
		if(disabledCommands.containsKey(guild.getLongID())){
			if(disabledCommands.get(guild.getLongID()).contains(getKeyFromCommand(base))){
				return true;
			}
		}
		
		return false;
	}
	
	public static void disableCommand(IGuild guild, DiscordCommand base){
		if(!base.canBeDisabled()) return;
		
		if(isCommandDisabled(guild, base)) return;
		
		if(!disabledCommands.containsKey(guild.getLongID())){
			disabledCommands.put(guild.getLongID(), new ArrayList<>());
		}
		
		disabledCommands.get(guild.getLongID()).add(getKeyFromCommand(base));
	}
	
	public static void enableCommand(IGuild guild, DiscordCommand base){
		if(!isCommandDisabled(guild, base)) return;
		disabledCommands.get(guild.getLongID()).remove(getKeyFromCommand(base));
		
		if(disabledCommands.get(guild.getLongID()).size() <= 0){
			disabledCommands.remove(guild.getLongID());
		}
	}
	
	public static DiscordCommand registerCommand( Class<? extends DiscordCommand> classObj, String key )
	{
		if(classObj == null) return null;
		
		try{
			DiscordCommand command = classObj.newInstance();
			
			if(command == null){
				return null;
			}
			
			String category = command.getCategory();
			
			if (!discordChatCommands.containsKey(key)) {
				discordChatCommands.put(key, command);
				if(category != null && !category.isEmpty()){
					commandCategory.put(key, category);
					
					if(!commandCategories.containsKey(category)){
						commandCategories.put(category, new CopyOnWriteArrayList<>());
					}
					commandCategories.get(category).add(key);
				}
				
				DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandRegisterEvent(command, key));
				return command;
			} else {
				System.err.println("Unable to register command with key: " + key + ", command already exists");
			}
			
		}catch (Exception e){
			DiscordBotBase.handleException(e);
		}
		
		return null;
	}
	
	public static void unRegisterCommand( String key )
	{
		if (discordChatCommands.containsKey(key)) {
			DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandRemoveEvent(discordChatCommands.get(key), key));
			discordChatCommands.remove(key);
		} else {
			System.err.println("Unable to remove no existing command with key: " + key);
		}
	}
}
