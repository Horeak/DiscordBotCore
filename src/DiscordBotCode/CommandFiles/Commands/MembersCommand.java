package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;

public class MembersCommand extends DiscordChatCommand {
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage ) {
		return "Shows current status of members in the server by showing total amount of users and online/offline count";
	}
	
	@Override
	public String commandPrefix() {
		return "members";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.withThumbnail(message.getGuild().getIconURL());
		builder.withColor(message.getAuthor().getColorForGuild(message.getGuild()));
		
		int online = 0;
		
		for(IUser user : message.getGuild().getUsers()){
			if(user.getPresence().getStatus() != StatusType.OFFLINE && user.getPresence().getStatus() != StatusType.INVISIBLE && user.getPresence().getStatus() != StatusType.UNKNOWN){
				online += 1;
			}
		}
		
		builder.appendField("Members", Integer.toString(message.getGuild().getUsers().size()), true);
		builder.appendField("Online", Integer.toString(online), true);
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args ) {
		return true;
	}
	
	@Override
	public boolean canCommandBePrivateChat() {
		return false;
	}
}
