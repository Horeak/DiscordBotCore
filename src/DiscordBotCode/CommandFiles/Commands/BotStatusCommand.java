package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Extra.TimeUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.Events.CommandInputEvents;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;

public class BotStatusCommand extends DevCommandBase {
	@Override
	public String commandPrefix() {
		return "status";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), getBuilder(message, args).build());
	}
	
	protected EmbedBuilder getBuilder(IMessage message, String[] args){
		EmbedBuilder builder = new EmbedBuilder();
		
		if(!message.getChannel().isPrivate()){
			builder.withColor(message.getAuthor().getColorForGuild(message.getGuild()));
		}else{
			builder.withColor(Color.red);
		}
		
		builder.withTitle("Status");
		builder.withDescription("Current status of `" + DiscordBotBase.discordClient.getOurUser().getName() + "`");
		
		int messageBacklog = 0;
		int subCommands = 0;
		
		for(DiscordChatCommand command : CommandUtils.discordChatCommands.values()){
			subCommands += command.subCommands.size();
		}
		
		for(CommandInputEvents.handleThreads th : CommandInputEvents.threadss){
			if(th.isAlive()){
				messageBacklog += th.messages.size();
			}
		}
		
		builder.appendField("Version", DiscordBotBase.getVersion(), true);
		builder.appendField("Dev mode", Boolean.toString(DiscordBotBase.devMode), true);
		builder.appendField("Debug mode", Boolean.toString(DiscordBotBase.debug), true);
		builder.appendField("Uptime", TimeUtil.getText("upTime", "<days> <hours> <mins> <secs>", false), true);
		builder.appendField("Commands executed", Integer.toString(CommandUtils.commandsExecuted), true);
		builder.appendField("Command sign", "\"" + getCommandSign(message.getChannel()).substring(1) + "\"", true);
		builder.appendField("Ping", Utils.getPing(message) + "ms", true);
		builder.appendField("Servers", Integer.toString(DiscordBotBase.discordClient.getGuilds().size()), true);
		builder.appendField("Message threads", CommandInputEvents.threadss.size() + " / " + CommandInputEvents.threads_amount, true);
		builder.appendField("Message backlog", Integer.toString(messageBacklog - 1), true); //Backlog amount - 1 because of current message
		builder.appendField("Commands", Integer.toString(CommandUtils.discordChatCommands.size()), true);
		builder.appendField("Sub-Commands", Integer.toString(subCommands), true);
		builder.appendField("Modules", Integer.toString(CommandUtils.discordModules.size()), true);
		
		builder.withThumbnail(DiscordBotBase.discordClient.getOurUser().getAvatarURL());
		
		return builder;
	}
}
