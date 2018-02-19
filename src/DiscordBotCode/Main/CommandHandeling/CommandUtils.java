package DiscordBotCode.Main.CommandHandeling;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.Events.CommandInputEvents;
import DiscordBotCode.Main.CustomEvents.CommandExecutedEvent;
import DiscordBotCode.Main.CustomEvents.CommandFailedExecuteEvent;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.DiscordModule;
import DiscordBotCode.Main.PermissionUtils;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandUtils
{
	public static int commandsExecuted = 0;
	
	public static ConcurrentHashMap<String, DiscordChatCommand> discordChatCommands = new ConcurrentHashMap<>(); //All commands added to the program
	public static ConcurrentHashMap<String, DiscordModule> discordModules = new ConcurrentHashMap<>();
	
	public static CopyOnWriteArrayList<ICommandFormatter> formatters = new CopyOnWriteArrayList<>();
	
	public static void executeCommand( IMessage m )
	{
		executeCommand(m, false);
	}
	
	/**
	 * @param internal Disables built in permissions system to allow commands to be excec<uted by the bot itself or from self constructed messages
	 */
	public static void executeCommand( IMessage m1T, boolean internal )
	{
		IMessage m1 = formatMessage(m1T);
		String text = m1.getContent();
		
		DiscordCommand command = getDiscordCommand(text, m1.getChannel());
		
		if (command != null) {
			String[] args = getArgsFromText(text, command, m1.getChannel());
			boolean permission = command.hasPermissions(m1, args) || internal;
			
			if (!command.canCommandBePrivateChat() && m1.getChannel().isPrivate()) {
				ChatUtils.sendMessage(m1.getChannel(), "*That command is not available for private chats!*");
				return;
			}
			
			if (command.getUserWhitelist() != null && !Arrays.asList(command.getUserWhitelist()).contains(m1.getAuthor().getStringID())) {
				ChatUtils.sendMessage(m1.getChannel(), "*You are not authorized to use that command!*");
				return;
			}
			
			
			if (permission) {
				if (command.canExecute(m1, args)) {//Execute the command and send it to the botBase
					try {
						command.commandExecuted(m1, args);
						commandsExecuted += 1;
					} catch (Exception e) {
						DiscordBotBase.handleException(e);
					}
					
					DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandExecutedEvent(command, m1));
					
					return; //End code on successful command execution
				} else {
					 if (DiscordBotBase.extraFeedback) {
						ChatUtils.sendMessage(m1.getChannel(), command.unableToExecuteCommand(m1));
					}
				}
			}else{
				if(DiscordBotBase.extraFeedback) {
					if(command.getRequiredRole(m1.getChannel()) != null){
						if(!PermissionUtils.hasRole(m1.getAuthor(), m1.getGuild(), command.getRequiredRole(m1.getChannel()), true)){
							ChatUtils.sendMessage(m1.getChannel(), m1.getAuthor().mention() + " You must be ` " + command.getRequiredRole(m1.getChannel()).getName() + " ` or above to use this command!");
						}
					}else {
						ChatUtils.sendMessage(m1.getChannel(), m1.getAuthor().mention() + " You do not have the required permissions to use this command!");
					}
				}
			}
		}
		
		DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandFailedExecuteEvent(command, m1));//Command failed from unknown error. Sending it to botBase
	}
	
	public static MessageObject getCurrentHandledMessage(){
		if(Thread.currentThread() instanceof CommandInputEvents.handleThreads){
			MessageObject ob = ((CommandInputEvents.handleThreads)Thread.currentThread()).curMessage;
			
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
				if(Modifier.isProtected(fiel.getModifiers())) { //All fields that are changeable are protected, therefor private fields can be used to unchangeable values
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
	
	private static DiscordChatCommand getCommand( String text, IChannel chat, boolean checkSign )
	{
		if (chat == null || text == null) {
			return null;
		}
		
		for (DiscordChatCommand command : discordChatCommands.values()) {
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
	
	private static DiscordSubCommand getSubCommandFromCommand( String text, DiscordCommand command, IChannel channel )
	{
		if (text == null || text.isEmpty() || command == null) {
			return null;
		}
		
		if (command instanceof DiscordChatCommand) {
			if (((DiscordChatCommand) command).subCommands.size() > 0) {
				for (DiscordSubCommand subCommand : ((DiscordChatCommand) command).subCommands) {
					if (subCommand == null) {
						continue;
					}
					
					if(compareCommandPrefix(text, subCommand, channel, false)){
						return subCommand;
					}
				}
			}
		}
		
		return null;
	}
	
	public static DiscordCommand getDiscordCommand( String text, IChannel channel )
	{
		DiscordCommand commandReturn = getCommand(text, channel, true);
		if (getSubCommandFromCommand(text, commandReturn, channel) != null) {
			commandReturn = getSubCommandFromCommand(text, commandReturn, channel);
		}
		
		return commandReturn;
	}
	
	public static DiscordCommand getCommandName(String text, IChannel channel){
		DiscordCommand commandReturn = getCommand(text, channel, false);
		if (getSubCommandFromCommand(text, commandReturn, channel) != null) {
			commandReturn = getSubCommandFromCommand(text, commandReturn, channel);
		}
		
		return commandReturn;
	}
	
	public static String[] getArgsFromText( String text, DiscordCommand command, IChannel channel )
	{
		String tempReplace = getCommandPrefix(text, command, channel, true);
		String temp = text.substring(tempReplace.length()); //TODO Find a better way incase there is mid sentence commands sometime
		
		if(temp.startsWith(" ")){
			temp = tempReplace.substring(1);
		}
		
		if(temp.isEmpty()){
			return new String[0];
		}
		
		String[] result = temp.split(" ");
		ArrayList<String> list = new ArrayList<>(Arrays.asList(result));
		list.removeIf(String::isEmpty);
		return list.toArray(new String[list.size()]);
	}
	
	private static boolean compareCommandPrefix(String text, DiscordCommand command, IChannel channel, boolean checkSign){
		return getCommandPrefix(text, command, channel, checkSign) != null;
	}
	
	
	private static String getCommandPrefix(String text, DiscordCommand command, IChannel channel, boolean checkSign){
		String commandPrefix = command instanceof DiscordChatCommand ? ((DiscordChatCommand)command).getCommandSign(channel) : command instanceof DiscordSubCommand ? ((DiscordSubCommand)command).baseCommand.getCommandSign(channel) : DiscordBotBase.getCommandSign();
		
		if(!checkSign){
			text = text.replace(commandPrefix, "");
			commandPrefix = "";
		}
		
		if(command instanceof DiscordSubCommand){
			DiscordSubCommand subCommand = (DiscordSubCommand)command;
			
			for(String prefix : subCommand.baseCommand.commandPrefixes()){
				String tempText = text;
				if(!command.caseSensitive()){
					prefix = prefix.toLowerCase();
					tempText = tempText.toLowerCase();
				}
				
				if(tempText.toLowerCase().startsWith(commandPrefix + prefix) || tempText.equals(commandPrefix + prefix)){
					commandPrefix += prefix + " ";
					break;
				}
				
				if(subCommand.baseCommand.fuzzyDetection()){
					if(tempText.contains(commandPrefix + prefix)){
						commandPrefix += prefix + " ";
						break;
					}
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
			
			if(text.length() > prefix.length() && !command.fuzzyDetection()){ //Hacky implementation
				prefix += " ";
			}
			
			if(tempText.startsWith(prefix) || tempText.equals(prefix)){
				return prefix;
			}
			
			if(command.fuzzyDetection()){
				if(tempText.contains(prefix)){
					return prefix;
				}
			}
		}
		
		return null;
	}
	
	public static String getKeyFromCommand(DiscordCommand command){
		if(command instanceof DiscordSubCommand){
			DiscordSubCommand subCommand = (DiscordSubCommand)command;
			
			for (Map.Entry<String, DiscordChatCommand> ent : discordChatCommands.entrySet()) {
				if (subCommand.baseCommand == ent.getValue()) {
					return ent.getKey() + ":" + subCommand.commandPrefix();
				}
			}
		}else {
			for (Map.Entry<String, DiscordChatCommand> ent : discordChatCommands.entrySet()) {
				if (command == ent.getValue()) {
					return ent.getKey();
				}
			}
		}
		
		return null;
	}
}
