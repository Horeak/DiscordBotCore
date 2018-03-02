package DiscordBotCode.CommandFiles.Commands;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IgnoredRolesCommand extends DiscordChatCommand {
	public IgnoredRolesCommand() {
		subCommands.add(new roles(this));
		subCommands.add(new addIgnore(this));
		subCommands.add(new removedIgnore(this));
	}
	
	@Override
	protected Permissions[] getRequiredPermissions() {
		return new Permissions[]{Permissions.ADMINISTRATOR};
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage ) {
		return "A regex system to allow ignoring specific roles from the permission system in the bot";
	}
	
	@Override
	public String commandPrefix() {
		return "ignoredRoles";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		if(!DiscordBotBase.data().get().getIgnoredRoles().containsKey(message.getGuild().getLongID())){
			ChatUtils.sendMessage(message.getChannel(), "No ignored roles for this server!");
			return;
		}
		
		ArrayList<EmbedBuilder> builders = new ArrayList<>();
		int cur = 0;
		
		for(String t : DiscordBotBase.data().get().getGuildIgnoredRoles(message.getGuild())){
			if(builders.size() <= cur || builders.get(cur) == null){
				builders.add(new EmbedBuilder());
				builders.get(cur).appendDescription("Ignored roles: ");
			}
			
			if(t != null && !t.isEmpty()){
				int amount = 0;
				
				for(IRole role : message.getGuild().getRoles()){
					if(role.getName().matches(t)){
						amount += 1;
					}
				}
				
				String tg = "\n`\"" + t + "\" - Role matches: " + amount + "`";
				
				if(builders.get(cur).build().description.length() + tg.length() >= (EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - 20)){
					builders.add(new EmbedBuilder());
					cur += 1;
					builders.get(cur).appendDescription("Ignored roles: ");
				}
				
				builders.get(cur).appendDescription(tg);
			}
		}
		
		for(EmbedBuilder builder : builders){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
		}
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

class removedIgnore extends DiscordSubCommand{
	public removedIgnore( DiscordChatCommand baseCommand ) {
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix() {
		return "remove";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage ) {
		return "Removes a regex string which is used to filter out roles for permissions on commands";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		String text = String.join(" ", args);
		
		for(String t : DiscordBotBase.data().get().getGuildIgnoredRoles(message.getGuild())){
			if(text.equalsIgnoreCase(t)){
				DiscordBotBase.data().get().getGuildIgnoredRoles(message.getGuild()).remove(t);
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The role ignore `" + text + "` has now been removed!");
			}
		}
		
		try {
			DiscordBotBase.data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args ) {
		return true;
	}
}

class addIgnore extends DiscordSubCommand{
	public addIgnore( DiscordChatCommand baseCommand ) {
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix() {
		return "add";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage ) {
		return "Adds a regex string which is used to filter out roles for permissions on commands";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		String text = String.join(" ", args);
		
		for(String t : DiscordBotBase.data().get().getGuildIgnoredRoles(message.getGuild())){
			if(text.equalsIgnoreCase(t)){
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This role ignore has already been added!");
				return;
			}
		}
		
		DiscordBotBase.data().get().getGuildIgnoredRoles(message.getGuild()).add(text);
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The role ignore `" + text + "` has now been added!");
		
		try {
			DiscordBotBase.data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args ) {
		return true;
	}
}

class roles extends DiscordSubCommand{
	public roles( DiscordChatCommand baseCommand ) {
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix() {
		return "roles";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args ) {
		List<IRole> roles = message.getGuild().getRoles();
		ArrayList<IRole> roles1 = PermissionUtils.filterRoles(message.getGuild(), roles);
		roles.removeAll(roles1);

		if(!DiscordBotBase.data().get().getIgnoredRoles().containsKey(message.getGuild().getLongID())){
			ChatUtils.sendMessage(message.getChannel(), "No ignored roles for this server!");
			return;
		}
		
		ArrayList<EmbedBuilder> builders = new ArrayList<>();
		int cur = 0;
		
		for(IRole role : roles){
			if(builders.size() <= cur || builders.get(cur) == null){
				builders.add(new EmbedBuilder());
				builders.get(cur).appendDescription("**Ignored roles:**");
			}
			
			if(role != null){
				String tg = "\n" + role.mention();
				
				if(builders.get(cur).build().description.length() + tg.length() >= (EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - 20)){
					builders.add(new EmbedBuilder());
					cur += 1;
					builders.get(cur).appendDescription("**Ignored roles:**");
				}
				
				builders.get(cur).appendDescription(tg);
			}
		}
		
		for(EmbedBuilder builder : builders){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args ) {
		return true;
	}
}
