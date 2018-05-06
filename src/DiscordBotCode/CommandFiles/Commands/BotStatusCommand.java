package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Extra.TimeUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;

@DiscordCommand
public class BotStatusCommand extends DiscordChatCommand
{
	@Override
	public String commandPrefix() {
		return "botinfo";
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
		
		builder.appendField("Version", DiscordBotBase.getVersion(), true);
		builder.appendField("Uptime", TimeUtil.getText("upTime", "<days> <hours> <mins> <secs>", false), true);
		builder.appendField("Ping", Utils.getPing(message) + "ms", true);
		builder.appendField("Servers", Integer.toString(DiscordBotBase.discordClient.getGuilds().size()), true);
		
		builder.withThumbnail(DiscordBotBase.discordClient.getOurUser().getAvatarURL());
		
		return builder;
	}
}
