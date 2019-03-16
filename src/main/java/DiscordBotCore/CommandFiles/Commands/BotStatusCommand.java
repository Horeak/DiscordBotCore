package DiscordBotCore.CommandFiles.Commands;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.DiscordBotBase;
import DiscordBotCore.Main.Utils;
import DiscordBotCore.Misc.Annotation.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.text.SimpleDateFormat;

@Command
public class BotStatusCommand extends DiscordCommand
{
	private static SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY");
	
	@Override
	public String commandPrefix() {
		return "botinfo";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Shows the status of the bot";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return super.getShortDescription(sourceCommand, callerMessage);
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "botinfo";
	}
	
	@Override
	public String getCategory()
	{
		return "info commands";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		EmbedBuilder builder = new EmbedBuilder();
		
		if(!message.getChannel().isPrivate()){
			builder.withColor(message.getAuthor().getColorForGuild(message.getGuild()));
		}else{
			builder.withColor(Color.red);
		}
		
		builder.withTitle("Status");
		builder.withDescription("Current status of `" + DiscordBotBase.discordClient.getOurUser().getName() + "`");
		
		builder.appendField("Version", DiscordBotBase.getVersion(), true);
		if(DiscordBotBase.getBuildDate() != null) builder.appendField("Version date", format.format(DiscordBotBase.getBuildDate()), true);
		
		builder.appendField("Uptime", DiscordBotBase.getUpTime(), true);
		builder.appendField("Ping", Utils.getPing(message) + "ms", true);
		builder.appendField("Web ping", Utils.getWebResponse() + "ms", true);
		
		builder.appendField("Servers", Integer.toString(DiscordBotBase.discordClient.getGuilds().size()), true);
		builder.appendField("Users", Integer.toString(DiscordBotBase.discordClient.getUsers().size()), true);
		
		builder.withThumbnail(DiscordBotBase.discordClient.getOurUser().getAvatarURL());
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
	}
}
