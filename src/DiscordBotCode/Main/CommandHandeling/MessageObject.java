package DiscordBotCode.Main.CommandHandeling;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageObject extends Message {
	private Guild guild;
	private Channel post_channel;
	private boolean deletable = true;
	
	public MessageObject(IMessage message){
		this((Message)message);
	}
	
	public MessageObject(Message message){
		super(message.getClient(), message.getLongID(), message.getContent(), message.getAuthor(), message.getChannel(), message.getTimestamp(), message.getEditedTimestamp().orElse(null), message.mentionsEveryone(), message.getRawMentionsLong(), message.getRawRoleMentionsLong(), message.getAttachments(), message.isPinned(), getRawEmbeds(message.getEmbeds()), message.getReactions(), message.getWebhookLongID());
	}
	
	public MessageObject( IDiscordClient client, long id, String content, IUser user, IChannel channel, LocalDateTime timestamp, LocalDateTime editedTimestamp, boolean mentionsEveryone, List<Long> mentions, List<Long> roleMentions, List<Attachment> attachments, boolean pinned, List<Embed> embeds, List<IReaction> reactions, long webhookID ) {
		super(client, id, content, user, channel, timestamp, editedTimestamp, mentionsEveryone, mentions, roleMentions, attachments, pinned, embeds, reactions, webhookID);
	}
	
	private static List<Embed> getRawEmbeds( List<IEmbed> embeds ){
		ArrayList<Embed> list = new ArrayList<>();
		embeds.forEach((e) -> {
			if(e instanceof Embed){
				list.add((Embed)e);
			}
		});
		
		return list;
	}
	
	@Override
	public IGuild getGuild() {
		return this.guild == null ? super.getGuild() : guild;
	}
	
	public Channel getPost_channel(){
		return post_channel;
	}
	
	@Override
	public void delete() {
		if(deletable) super.delete();
	}
}
