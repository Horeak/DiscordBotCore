package DiscordBotCode.Main.CommandHandeling.Events;

import DiscordBotCode.Misc.Annotation.EventListener;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;

public class DefaultEvents
{
	@EventListener
	public static void clientDisconnected(DisconnectedEvent event){
		System.out.println("Client disconnected for reason: " + event.getReason());
	}
	
	@EventListener
	public static void reconnectFailure(ReconnectFailureEvent event){
		System.out.println("Client reconnect attempt num " + event.getCurrentAttempt() + " failed!");
	}
	
	@EventListener
	public static void guildCreate(GuildCreateEvent event){
		System.out.println("Bot has joined server \"" + event.getGuild().getName() + "\"");
	}
}
