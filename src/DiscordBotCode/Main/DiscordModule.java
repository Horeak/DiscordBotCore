package DiscordBotCode.Main;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

public class DiscordModule implements IModule
{
	private String name, author, version, minVersion;
	private Class<? extends DiscordChatCommand>[] commands;
	
	public DiscordModule( String name, String author, String version, String minVersion, Class<? extends DiscordChatCommand>[] commands )
	{
		this.name = name;
		this.author = author;
		this.version = version;
		this.minVersion = minVersion;
		this.commands = commands;
	}
	
	@Override
	public boolean enable( IDiscordClient client )
	{
		System.out.println("Enabling module \"" + name + "\" version [" + version + "]");
		for (Class command : commands) {
			DiscordBotBase.registerCommand(command, command.getName());
		}
		
		return true;
	}
	
	@Override
	public void disable()
	{
		System.out.println("Disabling module \"" + name + "\"");
		for (Class command : commands) {
			DiscordBotBase.unRegisterCommand(command.getName() + "_key");
		}
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getAuthor()
	{
		return author;
	}
	
	@Override
	public String getVersion()
	{
		return version;
	}
	
	@Override
	public String getMinimumDiscord4JVersion()
	{
		return minVersion;
	}
}
