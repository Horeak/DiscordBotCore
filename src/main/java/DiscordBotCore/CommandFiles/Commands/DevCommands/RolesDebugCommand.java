package DiscordBotCore.CommandFiles.Commands.DevCommands;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.DeveloperSystem.DevCommandBase;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Misc.Annotation.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;

@Command
public class RolesDebugCommand extends DevCommandBase
{
	@Override
	public String commandPrefix()
	{
		return "roles";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Lists all role ids";
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
	public boolean commandPrivateChat()
	{
		return false;
	}
}
