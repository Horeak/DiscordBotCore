package DiscordBotCore.CommandFiles.Commands;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import DiscordBotCore.Main.PermissionUtils;
import DiscordBotCore.Misc.Annotation.Command;
import org.apache.commons.lang3.text.WordUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Command
public class ListCommandsCommand extends DiscordCommand
{
	
	//TODO Add a option to show commands avaliable for a specific role
	
	@Override
	public String getCategory()
	{
		return "info commands";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		int length = 0;
		int fieldCount = 0;
		
		String arg = String.join(" ", args);
		
		ConcurrentHashMap<String, CopyOnWriteArrayList<DiscordCommand>> commands = new ConcurrentHashMap<>();
		
		for (Map.Entry<String, DiscordCommand> ent : CommandUtils.discordChatCommands.entrySet()) {
			String category = CommandUtils.getCommandCategory(ent.getValue());
			String key = category != null ? category : "Default Commands";
			key = WordUtils.capitalize(key);
			
			if(!commands.containsKey(key)){
				commands.put(key, new CopyOnWriteArrayList<>());
			}
			
			commands.get(key).add(ent.getValue());
		}
		
		ArrayList<EmbedBuilder> builderArrayList = new ArrayList<>();
		builderArrayList.add(new EmbedBuilder());
		builderArrayList.get(0).withColor(message.getChannel().isPrivate() ? Color.darkGray : message.getAuthor().getColorForGuild(message.getGuild()));
		
		int num = 0;
		
		for (Map.Entry<String, CopyOnWriteArrayList<DiscordCommand>> ent : commands.entrySet()) {
			ArrayList<StringBuilder> builders = new ArrayList<>();
			
			if(arg != null && !arg.isEmpty()){
				if(!ent.getKey().equalsIgnoreCase(arg)){
					continue;
				}
			}
			
			int cur = 0;
			builders.add(new StringBuilder());
			
			for(DiscordCommand command : ent.getValue()) {
				if (!message.getChannel().isPrivate() || command.commandPrivateChat()) {
					if (command.isCommandVisible()) {
						if (command.hasPermissions(message, new String[]{}) && (!message.getChannel().isPrivate() || command.commandPrivateChat())) {
							String title = command.getCommandSign(message.getChannel()) + command.commandPrefix();
							String usage = command.getUsage(this, message);
							String description = command.getShortDescription(this, message);
							String text = "";
							
							
							if(usage != null) {
								usage = usage.replaceFirst("(?i)" + command.commandPrefix(), "");
							}
							
							if(usage != null) {
								if (usage.startsWith(" ")) {
									usage = usage.substring(1);
								}
							}
							
							
							text = "``" + title + (usage != null ? " " + usage : "") + "`` | " + (description != null ? description : "No Description");
							
							if(!message.getChannel().isPrivate() && message.getGuild() != null) {
								if (CommandUtils.isCommandDisabled(message.getGuild(), command)){
									if(PermissionUtils.hasPermissions(message.getAuthor(), message.getGuild(), message.getChannel(), EnumSet.of(Permissions.ADMINISTRATOR))) {
										text = "~~" + text + "~~";
									}else{
										continue;
									}
								}
							}
							
							if(builders.get(cur).length() + text.length() >= EmbedBuilder.FIELD_CONTENT_LIMIT){
								builders.add(new StringBuilder());
								cur += 1;
							}
							
							builders.get(cur).append("\n" + text);
						}
					}
				}
			}
			
			for(StringBuilder builder : builders){
				if (length >= EmbedBuilder.MAX_CHAR_LIMIT || (fieldCount + 1) >= EmbedBuilder.FIELD_COUNT_LIMIT) {
					num += 1;

					builderArrayList.add(new EmbedBuilder());
					builderArrayList.get(num).withColor(message.getChannel().isPrivate() ? Color.darkGray : message.getAuthor().getColorForGuild(message.getGuild()));

					length = 0;
					fieldCount = 0;
				}
				
				if(builder.toString() != null && !builder.toString().isEmpty()) {
					builderArrayList.get(num).appendField("**" + ent.getKey() + "**", builder.toString(), false);
					fieldCount += 1;
				}
			}
		}
		
		for (EmbedBuilder bd : builderArrayList) {
			ChatUtils.sendMessage(message.getChannel(), bd.build());
		}
	}

	@Override
	public String commandPrefix()
	{
		return "commands";
	}
	
	public boolean isCommandVisible()
	{
		
		return false;
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "commands [category]";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		
		return "Lists all commands available on the bot";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Lists all commands in the bot";
	}
	
}
