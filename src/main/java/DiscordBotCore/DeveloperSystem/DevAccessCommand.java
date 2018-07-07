package DiscordBotCore.DeveloperSystem;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.Utils;
import DiscordBotCore.Misc.Annotation.Command;
import DiscordBotCore.Misc.Annotation.SubCommand;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;

@Command
public class DevAccessCommand extends DevCommandBase
{
	@Override
	public String commandPrefix()
	{
		return "DevAccess";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Grant/Revoke dev status";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Use this command to grant or revoke dev access");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return DevAccess.isOwner(message.getAuthor().getLongID());
	}
	
	@SubCommand( parent = DevAccessCommand.class )
	public static class grant extends DiscordCommand
	{
		@Override
		public String commandPrefix()
		{
			return "grant";
		}
		
		@Override
		public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Grants dev status";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			ArrayList<Long> ids = new ArrayList<>();
			
			for (String t : args) {
				if (Utils.isLong(t)) {
					ids.add(Long.parseLong(t));
				}
			}
			
			for (IUser user : message.getMentions()) {
				ids.add(user.getLongID());
			}
			
			for (Long t : ids) {
				RequestBuffer.request(() -> {
					IUser user = message.getClient().fetchUser(t);
					
					if (user != null) {
						DevAccess.addDev(t);
						ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " \"" + user.getName() + "#" + user.getDiscriminator() + "\" has been granted dev status!");
					}
				});
				
			}
			
			if (ids.size() <= 0) {
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no users!");
			}
		}
		
		@Override
		public boolean canExecute( IMessage message, String[] args )
		{
			return DevAccess.isOwner(message.getAuthor().getLongID());
		}
	}
	
	@SubCommand( parent = DevAccessCommand.class )
	public static class revoke extends DiscordCommand
	{
		@Override
		public String commandPrefix()
		{
			return "revoke";
		}
		
		@Override
		public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Revoke dev status";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			ArrayList<Long> ids = new ArrayList<>();
			
			for (String t : args) {
				if (Utils.isInteger(t)) {
					ids.add(Long.parseLong(t));
				}
			}
			
			for (IUser user : message.getMentions()) {
				ids.add(user.getLongID());
			}
			
			for (Long t : ids) {
				if (!DevAccess.isOwner(t)) {
					DevAccess.removeDev(t);
				}
			}
			
			if (ids.size() > 0) {
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " " + ids.size() + " users had their dev status revoked!");
			} else {
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no users!");
			}
		}
		
		@Override
		public boolean canExecute( IMessage message, String[] args )
		{
			return DevAccess.isOwner(message.getAuthor().getLongID());
		}
	}
}