package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;

public class DevAccessCommand extends DevCommandBase
{
	public DevAccessCommand()
	{
		subCommands.add(new grant(this));
		subCommands.add(new revoke(this));
	}
	
	@Override
	public String commandPrefix()
	{
		return "DevAccess";
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
}

class grant extends DiscordSubCommand{
	public grant( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "grant";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<Long> ids = new ArrayList<>();
		
		for(String t : args){
			if(Utils.isLong(t)){
				ids.add(Long.parseLong(t));
			}
		}
		
		for(IUser user : message.getMentions()){
			ids.add(user.getLongID());
		}
		
		for(Long t : ids){
			RequestBuffer.request(() -> {
				IUser user = DiscordBotBase.discordClient.fetchUser(t);
				
				if(user != null){
					DevAccess.addDev(t);
					ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " \"" + user.getName() + "#" + user.getDiscriminator() +"\" has been granted dev status!");
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

class revoke extends DiscordSubCommand{
	public revoke( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "revoke";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<Long> ids = new ArrayList<>();
		
		for(String t : args){
			if(Utils.isInteger(t)){
				ids.add(Long.parseLong(t));
			}
		}
		
		for(IUser user : message.getMentions()){
			ids.add(user.getLongID());
		}
		
		for(Long t : ids){
			if(!DevAccess.isOwner(t)) {
				DevAccess.removeDev(t);
			}
		}
		
		if(ids.size() > 0) {
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " " + ids.size() + " users had their dev status revoked!");
		}else{
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no users!");
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return DevAccess.isOwner(message.getAuthor().getLongID());
	}
}