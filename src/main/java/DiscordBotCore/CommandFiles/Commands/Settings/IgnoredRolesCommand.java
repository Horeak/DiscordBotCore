package DiscordBotCore.CommandFiles.Commands.Settings;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.PermissionsUtils;
import DiscordBotCore.Misc.Annotation.Command;
import DiscordBotCore.Misc.Annotation.DataObject;
import DiscordBotCore.Misc.Annotation.SubCommand;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Command
public class IgnoredRolesCommand extends DiscordCommand
{
	@DataObject( file_path = "data.json", name = "ignoredRoles" )
	public static HashMap<Long, CopyOnWriteArrayList<String>> ignoredRoles = new HashMap<>();
	
	public static HashMap<Long, CopyOnWriteArrayList<String>> getIgnoredRoles()
	{
		return ignoredRoles;
	}
	
	public static CopyOnWriteArrayList<String> getGuildIgnoredRoles( IGuild guild )
	{
		if (!ignoredRoles.containsKey(guild.getLongID())) {
			ignoredRoles.put(guild.getLongID(), new CopyOnWriteArrayList<>());
		}
		return ignoredRoles.get(guild.getLongID());
	}
	
	@Override
	public EnumSet<Permissions> getRequiredPermissions()
	{
		return EnumSet.of(Permissions.ADMINISTRATOR);
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "A regex system to allow ignoring specific roles from the permission system in the bot";
	}
	
	@Override
	public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Filter out roles from role checks";
	}
	
	@Override
	public String commandPrefix()
	{
		return "ignoredRoles";
	}
	
	@Override
	public String getCategory()
	{
		return "settings";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if (!getIgnoredRoles().containsKey(message.getGuild().getLongID())) {
			ChatUtils.sendMessage(message.getChannel(), "No ignored roles for this server!");
			return;
		}
		
		ArrayList<EmbedBuilder> builders = new ArrayList<>();
		int cur = 0;
		
		for (String t : getGuildIgnoredRoles(message.getGuild())) {
			if (builders.size() <= cur || builders.get(cur) == null) {
				builders.add(new EmbedBuilder());
				builders.get(cur).appendDescription("Ignored roles: ");
			}
			
			if (t != null && !t.isEmpty()) {
				int amount = 0;
				
				for (IRole role : message.getGuild().getRoles()) {
					if (role.getName().matches(t)) {
						amount += 1;
					}
				}
				
				String tg = "\n`\"" + t + "\" - Role matches: " + amount + "`";
				
				if (builders.get(cur).build().description.length() + tg.length() >= (EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - 20)) {
					builders.add(new EmbedBuilder());
					cur += 1;
					builders.get(cur).appendDescription("Ignored roles: ");
				}
				
				builders.get(cur).appendDescription(tg);
			}
		}
		
		for (EmbedBuilder builder : builders) {
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
		}
	}
	
	@Override
	public boolean commandPrivateChat()
	{
		return false;
	}
	
	@SubCommand( parent = IgnoredRolesCommand.class )
	public static class removedIgnore extends DiscordCommand
	{
		@Override
		public String commandPrefix()
		{
			return "remove";
		}
		
		@Override
		public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Removes a regex string which is used to filter out roles for permissions on commands";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			String text = String.join(" ", args);
			
			for (String t : IgnoredRolesCommand.getGuildIgnoredRoles(message.getGuild())) {
				if (text.equalsIgnoreCase(t)) {
					IgnoredRolesCommand.getGuildIgnoredRoles(message.getGuild()).remove(t);
					ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The role ignore `" + text + "` has now been removed!");
				}
			}
			
		}
		
		@Override
		public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Exclude a role from checks";
		}
	}
	
	@SubCommand( parent = IgnoredRolesCommand.class )
	public static class addIgnore extends DiscordCommand
	{
		@Override
		public String commandPrefix()
		{
			return "add";
		}
		
		@Override
		public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Adds a regex string which is used to filter out roles for permissions on commands";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			String text = String.join(" ", args);
			
			for (String t : IgnoredRolesCommand.getGuildIgnoredRoles(message.getGuild())) {
				if (text.equalsIgnoreCase(t)) {
					ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This role ignore has already been added!");
					return;
				}
			}
			
			IgnoredRolesCommand.getGuildIgnoredRoles(message.getGuild()).add(text);
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The role ignore `" + text + "` has now been added!");
		}
		
		@Override
		public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Re-add a role for role checks";
		}
		
	}
	
	@SubCommand( parent = IgnoredRolesCommand.class )
	public static class roles extends DiscordCommand
	{
		@Override
		public String commandPrefix()
		{
			return "roles";
		}
		
		@Override
		public void commandExecuted( IMessage message, String[] args )
		{
			List<IRole> roles = message.getGuild().getRoles();
			ArrayList<IRole> roles1 = PermissionsUtils.filterRoles(message.getGuild(), roles);
			roles.removeAll(roles1);
			
			if (!IgnoredRolesCommand.getIgnoredRoles().containsKey(message.getGuild().getLongID())) {
				ChatUtils.sendMessage(message.getChannel(), "No ignored roles for this server!");
				return;
			}
			
			ArrayList<EmbedBuilder> builders = new ArrayList<>();
			int cur = 0;
			
			for (IRole role : roles) {
				if (builders.size() <= cur || builders.get(cur) == null) {
					builders.add(new EmbedBuilder());
					builders.get(cur).appendDescription("**Ignored roles:**");
				}
				
				if (role != null) {
					String tg = "\n" + role.mention();
					
					if (builders.get(cur).build().description.length() + tg.length() >= (EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - 20)) {
						builders.add(new EmbedBuilder());
						cur += 1;
						builders.get(cur).appendDescription("**Ignored roles:**");
					}
					
					builders.get(cur).appendDescription(tg);
				}
			}
			
			for (EmbedBuilder builder : builders) {
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
			}
		}
		
		@Override
		public String getShortDescription( DiscordCommand sourceCommand, IMessage callerMessage )
		{
			return "Lists all excluded roles";
		}
	}
}