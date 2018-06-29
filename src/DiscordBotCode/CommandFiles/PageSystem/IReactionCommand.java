package DiscordBotCode.CommandFiles.PageSystem;

import DiscordBotCode.Misc.CustomEntry;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface IReactionCommand
{
	Long timeout = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	ScheduledExecutorService timer = Executors.newScheduledThreadPool(0);
	
	void addReactions( IMessage message );
	void handleEvent( ReactionAddEvent event, IMessage command );
	
	boolean isValidReaction( ReactionEmoji emoji);
	
	default void sendMessage(IMessage command, IMessage message, IUser author){
		if(message == null) return;
		
		addReactions(message);
		
		ReactionEvent.messages.put(message.getLongID(), new CustomEntry<>(this, author.getLongID()));
		ReactionEvent.commands.put(message.getLongID(), command);
		
		timer.schedule(() -> clearReactions(message), timeout, TimeUnit.MILLISECONDS);
	}
	
	default void clearReactions(IMessage message){
		RequestBuffer.request(message::removeAllReactions);
		ReactionEvent.messages.remove(message.getLongID());
		ReactionEvent.commands.remove(message.getLongID());
	}
	
	default void incomingEvent( ReactionAddEvent event, Long user){
		if(event.getReaction() != null){
			if(event.getReaction().getEmoji() != null){
				
				IUser user1 = event.getClient().getUserByID(user);
				if(user1 != null) {
					RequestBuffer.request(() -> event.getMessage().removeReaction(user1, event.getReaction()));
					
					if (isValidReaction(event.getReaction().getEmoji()) && event.getUser().getLongID() == user) {
						handleEvent(event, ReactionEvent.commands.get(event.getMessageID()));
					}
				}
			}
		}
	}
	
}
