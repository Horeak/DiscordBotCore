package DiscordBotCore.CommandFiles.Commands.Settings;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import DiscordBotCore.Misc.Annotation.Command;
import DiscordBotCore.Misc.Annotation.SubCommand;
import org.apache.commons.lang3.text.WordUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;
import java.util.EnumSet;

@Command
public class CommandToggle extends DiscordCommand
{
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Enable/Disable commands in current server";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "cmd <enable/disable> <command/category>";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) { }
	
	public static void toggle( IMessage message, String text, boolean state )
	{
		ArrayList<DiscordCommand> cmds = CommandUtils.getCommandsFromCategory(text);
		
		if(cmds.size() > 0){
			for(DiscordCommand command : cmds){
				if(!command.canBeDisabled()){
					continue;
				}
				
				if(state) {
					CommandUtils.enableCommand(message.getGuild(), command);
				}else{
					CommandUtils.disableCommand(message.getGuild(), command);
				}
			}
			
			ChatUtils.sendMessage(message.getChannel(), "Category \"" + WordUtils.capitalize(text) + "\" has now been " + (state ? "Enabled" : "Disabled"));
		}else{
			DiscordCommand command = CommandUtils.getDiscordCommand(text, message.getChannel(), false);
			
			if(command != null) {
				if(!command.canBeDisabled()){
					ChatUtils.sendMessage(message.getChannel(), "Command \"" + WordUtils.capitalize(text) + "\" is unable to be disabled!");
					return;
				}
				
				if (state) {
					CommandUtils.enableCommand(message.getGuild(), command);
				} else {
					CommandUtils.disableCommand(message.getGuild(), command);
				}
				
				ChatUtils.sendMessage(message.getChannel(), "Command \"" + WordUtils.capitalize(text) + "\" has now been " + (state ? "Enabled" : "Disabled"));
			}else{
				ChatUtils.sendMessage(message.getChannel(), "Found no category or command with name \"" + WordUtils.capitalize(text) + "\"");
			}
		}
	}
	
	//TODO Make these into sub commands of Settings instead?
	
	@SubCommand(parent = CommandToggle.class)
	public static class cmdEnable extends DiscordCommand
	{
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			String text = String.join(" ", args);
			toggle(message, text, true);
		}
		
		@Override
		public String commandPrefix()
		{
			return "enable";
		}
		
		@Override
		public boolean canBeDisabled()
		{
			return false;
		}
	}
	
	@SubCommand(parent = CommandToggle.class)
	public static class cmdDisable extends DiscordCommand
	{
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			String text = String.join(" ", args);
			toggle(message, text, false);
		}
		
		@Override
		public String commandPrefix()
		{
			return "disable";
		}
		
		@Override
		public boolean canBeDisabled()
		{
			return false;
		}
	}
	
	@Override
	public String commandPrefix()
	{
		return "cmd";
	}
	
	@Override
	public EnumSet<Permissions> getRequiredPermissions()
	{
		return EnumSet.of(Permissions.ADMINISTRATOR);
	}
	
	@Override
	public boolean commandPrivateChat()
	{
		return false;
	}
	
	@Override
	public String getCategory()
	{
		return "settings";
	}

	@Override
	public boolean canBeDisabled()
	{
		return false;
	}
}
