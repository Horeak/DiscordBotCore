package DiscordBotCode.Main;

import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.MessageObject;
import DiscordBotCode.Misc.Requests;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( { "unused", "SameParameterValue" } )
public class ChatUtils
{
	public static Message createMessage( String text, IChannel channel, List<IMessage.Attachment> attachments, List<Embed> embeds, List<IReaction> reactions )
	{
		return createMessage(text, channel, false, attachments, embeds, reactions, 0L);
	}
	
	public static Message createMessage( String text, IChannel channel, List<IMessage.Attachment> attachments, List<Embed> embeds )
	{
		return createMessage(text, channel, false, attachments, embeds, null, 0L);
	}
	
	public static Message createMessage( String text, IChannel channel, List<IMessage.Attachment> attachments )
	{
		return createMessage(text, channel, false, attachments, null, null, 0L);
	}
	
	public static Message createMessage( String text, IChannel channel, boolean pinned, List<IMessage.Attachment> attachments, List<Embed> embeds, List<IReaction> reactions, Long webhookID, ArrayList<Long> mentions )
	{
		return createMessage(DiscordBotBase.discordClient.getOurUser(), text, channel, pinned, attachments, embeds, reactions, webhookID, mentions);
	}
	
	public static Message createMessage( IUser user, String text, IChannel channel, boolean pinned, List<IMessage.Attachment> attachments, List<Embed> embeds, List<IReaction> reactions, Long webhookID, ArrayList<Long> mentions )
	{
		//Create a fake message instance
		if (text == null || text.isEmpty()) {
			return null;
		}
		MessageTokenizer tokenizer = new MessageTokenizer(DiscordBotBase.discordClient, text);
		List<Long> roleMentions = new ArrayList<>();
		
		while (tokenizer.hasNextMention()) {
			MessageTokenizer.MentionToken tt = tokenizer.nextMention();
			if (tt.getMentionObject() instanceof IRole) {
				roleMentions.add(tt.getMentionObject().getLongID());
			}
		}
		
		return new Message(DiscordBotBase.discordClient,                   //client
				getMessageSnowflake(channel),                              //id
				text,                                                      //content
				user,                 //user
				channel,                                                   //channel
				Instant.now(),                                       //timestamp
				null,                                       //editedTimestamp
				text.contains("@everyone"),                                //mentionsEveryone
				mentions,                                                  //mentions
				roleMentions,                                              //roleMentions
				attachments,                                               //attachments
				pinned,                                             //pinned
				embeds,
				webhookID,//webhook
				IMessage.Type.DEFAULT
				);
	}
	
	private static Message createMessage( String text, IChannel channel, boolean pinned, List<IMessage.Attachment> attachments, List<Embed> embeds, List<IReaction> reactions, Long webhookID )
	{ //Create a fake message instance
		if (text == null || text.isEmpty()) {
			return null;
		}
		
		MessageTokenizer tokenizer = new MessageTokenizer(DiscordBotBase.discordClient, text);
		ArrayList<Long> mentions = new ArrayList<>();
		
		while (tokenizer.hasNextMention()) {
			MessageTokenizer.MentionToken tt = tokenizer.nextMention();
			
			if (tt.getMentionObject() instanceof IUser) {
				mentions.add(tt.getMentionObject().getLongID());
				
			}
		}
		return createMessage(text, channel, pinned, attachments, embeds, reactions, webhookID, mentions);
	}
	
	//Creates a random id for fake messages which should hopefully not interfere with actual discord message ids
	private static Long getMessageSnowflake( IChannel channel )
	{
		return System.currentTimeMillis() + Utils.rand.nextLong();
	}
	
	public static IMessage sendMessage( IChannel chat, String message, MessageBuilder.Styles styles, boolean useTTS, boolean buffer )
	{
		if(chat == null){
			return null;
		}
		
		if(chat.isPrivate() && ((PrivateChannel)chat).getRecipient() != null){
			if(((PrivateChannel)chat).getRecipient().isBot()){
				return null;
			}
		}
		
		MessageObject ob = CommandUtils.getCurrentHandledMessage();
		
		if(ob != null){
			if(ob.getPost_channel() != null){
				chat = ob.getPost_channel();
			}
		}
		
		final IMessage[] message1 = { null };
		
		Message messageObject = new Message(DiscordBotBase.discordClient, getMessageSnowflake(chat), message, DiscordBotBase.discordClient.getOurUser(), chat, null, null, false, new ArrayList<>(), new ArrayList<>(), null, false, null, 0, IMessage.Type.DEFAULT);
		if (messageObject != null && messageObject.getContent() != null && !messageObject.getContent().isEmpty()) {
			MessageBuilder builder = new MessageBuilder(DiscordBotBase.discordClient);
			builder.withChannel(chat);
			
			if(styles != null) {
				builder.withContent(message, styles);
			}else{
				builder.withContent(message);
			}
			
			if(useTTS) builder.withTTS();
			
			Requests.executeRequest(() -> {
				if (builder.getContent() != null && !builder.getContent().isEmpty() && builder.getChannel() != null) {
					message1[ 0 ] = builder.build();
				}
				
				return true;
			}, buffer);
		}
		
		return message1[0];
	}
	
	public static IMessage sendMessage( IMessage messageIn, MessageBuilder.Styles styles, boolean useTTS )
	{
		MessageBuilder builder = new MessageBuilder(DiscordBotBase.discordClient);
		
		if(CommandUtils.getCurrentHandledMessage() != null && CommandUtils.getCurrentHandledMessage().getPost_channel() != null){
			builder.withChannel(CommandUtils.getCurrentHandledMessage().getPost_channel());
		}else {
			builder.withChannel(messageIn.getChannel());
		}
		
		builder.appendContent(messageIn.getContent());
		
		if(useTTS)builder.withTTS();
		
		final IMessage[] message = new IMessage[ 1 ];
		
		RequestBuffer.request(() -> {
			try {
				message[ 0 ] = builder.build();
			} catch (DiscordException | MissingPermissionsException e) {
				DiscordBotBase.handleException(e);
			}
		});
		
		return message[ 0 ];
	}
	
	public static IMessage sendMessage( IChannel chat, String message, MessageBuilder.Styles styles )
	{
		return sendMessage(chat, message, styles, false, true);
	}
	
	public static IMessage sendMessage( IChannel channel, String title, EmbedObject object, boolean tts){
		MessageObject ob = CommandUtils.getCurrentHandledMessage();
		
		if(ob != null){
			if(ob.getPost_channel() != null){
				channel = ob.getPost_channel();
			}
		}
		
		final IMessage[] message = new IMessage[ 1 ];
		IChannel finalChannel = channel;
		RequestBuffer.request(() -> {
			try {
				message[ 0 ] = finalChannel.sendMessage(title, object, tts);
			} catch (DiscordException | MissingPermissionsException e) {
				DiscordBotBase.handleException(e);
			}
		});
		
		return message[ 0 ];
	}
	
	public static IMessage sendMessage( IChannel channel, String title, EmbedObject object){
		return sendMessage(channel, title, object, false);
	}
	
	public static IMessage sendMessage( IChannel channel, EmbedObject object){
		return sendMessage(channel, null, object, false);
	}
	
	public static  IMessage sendMessage( IChannel chat, String message )
	{
		return sendMessage(chat, message, (MessageBuilder.Styles) null);
	}
	
	public static void incomingMessageHandle( IMessage message )
	{
		CommandUtils.executeCommand(message);
	}
	
	public static void incomingPrivateMessageHandle( IMessage message )
	{
		CommandUtils.executeCommand(message);
	}
	
	public static IVoiceChannel getConnectedBotChannel( IGuild guild )
	{
		for (IVoiceChannel channel : DiscordBotBase.discordClient.getConnectedVoiceChannels()) {
			if (channel.getGuild() == guild) {
				return channel;
			}
		}
		
		return null;
	}
	
	public static IVoiceChannel getVoiceChannelFromUser( IUser user, IGuild guild )
	{
		if(user.getVoiceStateForGuild(guild) != null){
			if(user.getVoiceStateForGuild(guild).getChannel() != null){
				return user.getVoiceStateForGuild(guild).getChannel();
			}
		}
		return null;
	}
	
}


