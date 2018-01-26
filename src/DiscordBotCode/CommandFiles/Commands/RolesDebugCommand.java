package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.DeveloperSystem.DevCommandBase;
import DiscordBotCode.Main.ChatUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;

public class RolesDebugCommand extends DevCommandBase
{
	@Override
	public String commandPrefix()
	{
		return "roles";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<StringBuilder> builders = new ArrayList<>();
		builders.add(new StringBuilder());
		
		int num = 0;
		
		for(IRole role : message.getGuild().getRoles()){
			String text = "[" + role.getPosition() + "] " + role.getName() + " : " + role.getLongID();
			
			if(builders.get(num).length() + text.length() >= 1800){
				builders.add(new StringBuilder());
				num += 1;
			}
			
			builders.get(num).append(text + "\n");
		}
		
		for(StringBuilder builder : builders){
			ChatUtils.sendMessage(message.getAuthor().getOrCreatePMChannel(), "```" + builder.toString() + "```");
		}
		
		RequestBuffer.request(message::delete);
	}
	
	@Override
	public boolean canCommandBePrivateChat()
	{
		return false;
	}
}
