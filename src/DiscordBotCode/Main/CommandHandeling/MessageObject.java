package DiscordBotCode.Main.CommandHandeling;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageObject extends Message {
	protected Guild guild;
	protected Channel post_channel;
	protected boolean deletable = true;
	
	private ChannelObject channelObject;
	
	public MessageObject(IMessage message){
		this((Message)message);
	}
	
	public MessageObject(Message message){
		super(message.getClient(),
				message.getLongID(),
				message.getContent(),
				message.getAuthor(),
				message.getChannel(),
				message.getTimestamp(),
				message.getEditedTimestamp().orElse(null),
				message.mentionsEveryone(),
				message.getRawMentionsLong(),
				message.getRawRoleMentionsLong(),
				message.getAttachments(),
				message.isPinned(),
				getRawEmbeds(message.getEmbeds()),
				message.getWebhookLongID(),
				message.getType());
		
		channelObject = new ChannelObject(message.getChannel());
		channelObject.source = this;
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
	
	@Override
	public IChannel getChannel() {
		return channelObject;
	}
	
	public Channel getPost_channel(){
		return post_channel;
	}
	
	@Override
	public void delete() {
		if(deletable) super.delete();
	}
}
