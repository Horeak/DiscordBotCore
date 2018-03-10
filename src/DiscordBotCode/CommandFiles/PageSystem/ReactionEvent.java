package DiscordBotCode.CommandFiles.PageSystem;

import DiscordBotCode.Misc.CustomEntry;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ReactionEvent implements IListener<ReactionAddEvent>
{
	public static ConcurrentHashMap<Long, CustomEntry<IReactionCommand, Long>> messages = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Long, IMessage> commands = new ConcurrentHashMap<>();
	
	@Override
	public void handle( ReactionAddEvent event ) {
		if(event.getMessage() == null) return;
		
		if(event.getAuthor() != null && event.getMessage().getAuthor() != null) {
			if (messages.containsKey(event.getMessageID())) {
				messages.get(event.getMessageID()).getKey().incomingEvent(event, messages.get(event.getMessageID()).getValue());
			}
		}
	}
}
