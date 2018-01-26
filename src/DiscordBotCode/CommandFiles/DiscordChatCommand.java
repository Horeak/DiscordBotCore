package DiscordBotCode.CommandFiles;

import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.ServerSettings;
import sx.blah.discord.handle.obj.IChannel;

import java.util.ArrayList;

public abstract class DiscordChatCommand extends DiscordCommand
{
	public ArrayList<DiscordSubCommand> subCommands = new ArrayList<>();
	
	public String getCommandSign( IChannel channel )
	{
		if(channel == null || channel != null && channel.isPrivate()){
			return DiscordBotBase.getCommandSign();
		}else if(ServerSettings.valueExsists(channel.getGuild(), "prefix")){
			return ServerSettings.getValue(channel.getGuild(), "prefix");
		}
		return DiscordBotBase.getCommandSign();
		
	}
}
