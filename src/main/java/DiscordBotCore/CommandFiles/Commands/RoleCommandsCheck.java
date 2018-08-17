package DiscordBotCore.CommandFiles.Commands;

import DiscordBotCore.CommandFiles.Commands.Settings.IgnoredRolesCommand;
import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.DeveloperSystem.DevCommandBase;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import DiscordBotCore.Misc.Annotation.Command;
import org.apache.commons.lang3.text.WordUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;

@Command
public class RoleCommandsCheck extends DiscordCommand
{
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Shows available commands for specific roles";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "cmdRoleCheck <role>";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		String t = String.join("_", args);
		
		ArrayList<IRole> roles = new ArrayList<>();
		roles.addAll(message.getRoleMentions());
		
		for(IRole rol : message.getGuild().getRoles()){
			if(t.toLowerCase().contains(rol.getName().toLowerCase().replace(" ", "_"))){
				if(!roles.contains(rol)) {
					roles.add(rol);
				}
				
				t = t.replaceFirst("(?i)" + rol.getName().replace(" ", "_"), "");
			}
		}
		
		ArrayList<StringBuilder> builders = new ArrayList<>();
		int cur = 0;
		
		builders.add(new StringBuilder());
		
		top:
		for(IRole role : roles){
			CopyOnWriteArrayList<String> tg = IgnoredRolesCommand.getGuildIgnoredRoles(message.getGuild());
			
			for(String th : tg){
				if(role.getName().matches(th)){
					continue top;
				}
			}
			
			StringJoiner joiner = new StringJoiner("\n\t");
			joiner.add("");
			
			for (DiscordCommand ent : CommandUtils.discordChatCommands.values()) {
				if(ent.getCategory() != null && ent.getCategory().contains("dev") || ent instanceof DevCommandBase) continue;
				if(!ent.isCommandVisible()) continue;
				if(message.getGuild() != null && CommandUtils.isCommandDisabled(message.getGuild(), ent)) continue;
				
				if(ent.hasPermissions(message, role)){
					joiner.add("\"" + ent.getCommandSign(message.getChannel()) + WordUtils.capitalize(ent.commandPrefix()) + "\"");
				}
			}
			
			String text = "Role: #" + role.getName() + "\n > Commands: " + joiner.toString() + "\n";
			
			if(builders.get(cur).length() + text.length() >= 1900){
				builders.add(new StringBuilder());
				cur += 1;
			}
			
			builders.get(cur).append(text + "\n");
		}
		boolean posted = false;
		int i = 0;
		
		for(StringBuilder builder : builders) {
			if(builder.toString() != null && !builder.toString().isEmpty()) {
				posted = true;
				ChatUtils.sendMessage(message.getChannel(), (i == 0 ? message.getAuthor().mention() + "" : "") + "```perl\n" + builder.toString() + "```");
				i++;
			}
		}
		
		if(!posted){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no info on the specific role(s), role(s) may be on ignored list");
		}
	}
	
	@Override
	public String commandPrefix()
	{
		return "cmdRoleCheck";
	}
	
	@Override
	public boolean commandPrivateChat()
	{
		return false;
	}
	
	@Override
	public String getCategory()
	{
		return "info commands";
	}
	
	@Override
	public EnumSet<Permissions> getRequiredPermissions()
	{
		return EnumSet.of(Permissions.ADMINISTRATOR);
	}
}
