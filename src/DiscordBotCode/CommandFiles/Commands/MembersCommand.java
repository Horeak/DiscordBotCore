package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.CommandBase;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Annotation.DiscordCommand;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;

@DiscordCommand
public class MembersCommand extends DiscordChatCommand {
	@Override
	public String getDescription( CommandBase sourceCommand, IMessage callerMessage ) {
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
		
		int members = message.getGuild().getUsers().size();
		int number = online;
		
		float per = (float)number / (float)members;
		
		builder.withAuthorIcon(DiscordBotBase.discordClient.getOurUser().getAvatarURL());
		builder.withAuthorName(DiscordBotBase.discordClient.getOurUser().getDisplayName(message.getGuild()));
		
		builder.withTimestamp(System.currentTimeMillis());
		builder.withFooterText((int)(per * 100) + "% Online");
		
		builder.withDescription("```ml\nMembers: " + members + "\nOnline:  " + number + "```");

		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
	}
	

	
	@Override
	public boolean commandPrivateChat() {
		return false;
	}
}
