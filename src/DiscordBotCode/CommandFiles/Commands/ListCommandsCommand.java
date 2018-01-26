package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class ListCommandsCommand extends DiscordChatCommand
{
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		int length = 0;
		int fieldCount = 0;
		
		String starter = "```markdown\n";
		
		ArrayList<EmbedBuilder> builderArrayList = new ArrayList<>();
		builderArrayList.add(new EmbedBuilder());
		builderArrayList.get(0).withColor(message.getChannel().isPrivate() ? Color.yellow : message.getAuthor().getColorForGuild(message.getGuild()));
		
		int num = 0;
		
		for (Map.Entry<String, DiscordChatCommand> ent : CommandUtils.discordChatCommands.entrySet()) {
			DiscordChatCommand command = ent.getValue();
			
			if (!message.getChannel().isPrivate() || command.canCommandBePrivateChat()) {
				if (command.listCommand()) {
					if (command.hasPermissions(message, new String[]{}) && (!message.getChannel().isPrivate() || command.canCommandBePrivateChat())) {
						String title = getCommandSign(message.getChannel()) + command.commandPrefix() + "\n";
						String usage = command.getUsage(this, message);
						String description = command.getDescription(this, message);
						String text = "";
						
						if (usage != null) {
							usage = getCommandSign(message.getChannel()) + usage;
						}
						
						if(description != null) text += "> " + command.getDescription(this, message) + "\n\n";
						if(usage != null) text += "* Usage: " + usage + "\n";
						if(command.commandPrefixes().length > 1) text += "* Prefixes: " + String.join(", ", command.commandPrefixes()) + "\n";
						
						if(title.length() > EmbedBuilder.TITLE_LENGTH_LIMIT){
							title = title.substring(0, EmbedBuilder.TITLE_LENGTH_LIMIT - 4) + "...";
						}
						
						if(text.length() > EmbedBuilder.FIELD_CONTENT_LIMIT){
							text = text.substring(0, EmbedBuilder.FIELD_CONTENT_LIMIT - 4) + "...";
						}
						
						length += (title.length() + text.length());
						
						if (length >= EmbedBuilder.MAX_CHAR_LIMIT || (fieldCount + 1) >= EmbedBuilder.FIELD_COUNT_LIMIT) {
							num += 1;
							
							builderArrayList.add(new EmbedBuilder());
							builderArrayList.get(num).withColor(message.getChannel().isPrivate() ? Color.yellow : message.getAuthor().getColorForGuild(message.getGuild()));
							
							length = 0;
							fieldCount = 0;
						}
						
						if(text == null || text.isEmpty()){
							text = "Found no information.";
						}
						
						builderArrayList.get(num).appendField(title, starter + text + "```", false);
						fieldCount += 1;
					}
				}
			}
		}
		
		for (EmbedBuilder bd : builderArrayList) {
			ChatUtils.sendMessage(message.getAuthor().getOrCreatePMChannel(), bd.build());
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		
		return true;
	}
	
	@Override
	public String commandPrefix()
	{
		
		return "commands";
	}
	public boolean listCommand()
	{
		
		return false;
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		
		return "Lists all commands available on the bot";
	}
}
