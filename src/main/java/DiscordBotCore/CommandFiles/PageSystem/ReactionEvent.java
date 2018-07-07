package DiscordBotCore.CommandFiles.PageSystem;

import DiscordBotCore.Misc.Annotation.EventListener;
import DiscordBotCore.Misc.CustomEntry;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ReactionEvent
{
	public static ConcurrentHashMap<Long, CustomEntry<IReactionCommand, Long>> messages = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Long, IMessage> commands = new ConcurrentHashMap<>();
	
	@EventListener
	public static void handle( ReactionAddEvent event ) {
		if(event.getMessage() == null) return;
		
		if(event.getAuthor() != null && event.getMessage().getAuthor() != null) {
			if (messages.containsKey(event.getMessageID())) {
				messages.get(event.getMessageID()).getKey().incomingEvent(event, messages.get(event.getMessageID()).getValue());
			}
		}
	}
}
