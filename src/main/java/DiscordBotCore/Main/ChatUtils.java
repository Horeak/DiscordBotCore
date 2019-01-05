package DiscordBotCore.Main;

import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import DiscordBotCore.Main.CommandHandeling.MessageObject;
import DiscordBotCore.Misc.Requests;
import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
		return createMessage(channel.getClient().getOurUser(), text, channel, pinned, attachments, embeds, reactions, webhookID, mentions);
	}
	
	public static Message createMessage( IUser user, String text, IChannel channel, boolean pinned, List<IMessage.Attachment> attachments, List<Embed> embeds, List<IReaction> reactions, Long webhookID, ArrayList<Long> mentions )
	{
		//Create a fake message instance
		if (text == null || text.isEmpty()) {
			return null;
		}
		MessageTokenizer tokenizer = new MessageTokenizer(channel.getClient(), text);
		List<Long> roleMentions = new ArrayList<>();
		
		while (tokenizer.hasNextMention()) {
			MessageTokenizer.MentionToken tt = tokenizer.nextMention();
			if (tt.getMentionObject() instanceof IRole) {
				roleMentions.add(tt.getMentionObject().getLongID());
			}
		}
		
		return new Message(channel.getClient(),                   //client
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
		
		MessageTokenizer tokenizer = new MessageTokenizer(channel.getClient(), text);
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
	
	//TODO Schedule send message if shard isnt ready
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
		
		Message messageObject = new Message(chat.getClient(), getMessageSnowflake(chat), message, chat.getClient().getOurUser(), chat, null, null, false, new ArrayList<>(), new ArrayList<>(), null, false, null, 0, IMessage.Type.DEFAULT);
		if (messageObject != null && messageObject.getContent() != null && !messageObject.getContent().isEmpty()) {
			MessageBuilder builder = new MessageBuilder(chat.getClient());
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
	
	
	public static IMessage sendMessageWithUrlHandel(IChannel chat, String message){
		if(chat == null){
			return null;
		}
		
		if(chat.isPrivate() && ((PrivateChannel)chat).getRecipient() != null){
			if(((PrivateChannel)chat).getRecipient().isBot()){
				return null;
			}
		}
		
		MessageObject ob = CommandUtils.getCurrentHandledMessage();
		EmbedObject object = null;
		
		if(ob != null){
			if(ob.getPost_channel() != null){
				chat = ob.getPost_channel();
			}
		}
		
		List<String> urls = Utils.extractUrls(message);
		
		for(String t : urls){
			message = message.replace(t, "<" + t + ">");
		}
		
		if(urls.size() > 0){
			String tg = urls.get(0);
			
			EmbedBuilder builder = new EmbedBuilder();
			URL ur = null;
			
			try {
				ur = new URL(tg);
			} catch (MalformedURLException e) {
				DiscordBotBase.handleException(e);
			}
			
			try {
				String html = IOUtils.toString(ur.openStream(), "utf-8");//TODO This line causes a SSLHandshakeException
				Document doc = Jsoup.parseBodyFragment(html);
				
				Elements titleEl = doc.select("meta[property=og:title]");
				Elements descriptionEl = doc.select("meta[property=og:description]");
				Elements imageEl = doc.select("meta[property=og:image]");
				Elements color = doc.select("meta[name=theme-color]");
				Elements siteName = doc.select("meta[name=og:site_name]");
				
				Elements element = doc.select("link[href~=.*\\.(ico|png)]");
				
				if(titleEl.size() > 0){
					String title = titleEl.attr("content");
					
					if(title.length() > EmbedBuilder.AUTHOR_NAME_LIMIT){
						title = title.substring(0, (EmbedBuilder.AUTHOR_NAME_LIMIT - 4)) + "...";
					}
					
					builder.appendDescription("[" + title + "](" + tg + ")\n");
				}
				
				if(descriptionEl.size() > 0){
					String description = descriptionEl.attr("content").replace("\n", "").replace("  ", "");
					
					if(description.length() > EmbedBuilder.DESCRIPTION_CONTENT_LIMIT){
						description = description.substring(0, (EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - 4)) + "...";
					}
					
					if(!description.isEmpty()) {
						builder.appendDescription(description);
					}
				}
				
				if(color.size() > 0){
					String c1 = color.attr("content");
					Color c = Color.decode(c1);
					
					if(c != null){
						builder.withColor(c);
					}
				}
				
				if(imageEl.size() > 0){
					String image = imageEl.attr("content");
					boolean small = true;
					
					URL url = new URL(image);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
					
					BufferedImage bimg = ImageIO.read(connection.getInputStream());
					int width          = bimg.getWidth();
					int height         = bimg.getHeight();
					
					if(width > 700 || height > 700){
						small = false;
					}
					
					if(small){
						builder.withThumbnail(image);
					}else{
						builder.withImage(image);
					}
				}
				
				object = builder.build();
				
			} catch (IOException e) {
				DiscordBotBase.handleException(e);
			}
		}
		
		
		final IMessage[] message1 = { null };
		MessageBuilder builder = new MessageBuilder(chat.getClient());
		builder.withChannel(chat);
		
		builder.withContent(message);
		if(object != null) builder.withEmbed(object);
		
		RequestBuffer.request(() -> {
			if (builder.getContent() != null && !builder.getContent().isEmpty() && builder.getChannel() != null) {
				message1[ 0 ] = builder.build();
			}
		});
		
		return message1[0];
	}
	
	public static IMessage sendMessage( IMessage messageIn, MessageBuilder.Styles styles, boolean useTTS){
		return sendMessage(messageIn, styles, useTTS, null);
	}
	
	
	public static IMessage sendMessage( IMessage messageIn, MessageBuilder.Styles styles, boolean useTTS, EmbedObject object )
	{
		MessageBuilder builder = new MessageBuilder(messageIn.getClient());
		
		if(CommandUtils.getCurrentHandledMessage() != null && CommandUtils.getCurrentHandledMessage().getPost_channel() != null){
			builder.withChannel(CommandUtils.getCurrentHandledMessage().getPost_channel());
		}else {
			builder.withChannel(messageIn.getChannel());
		}
		
		builder.appendContent(messageIn.getContent());
		
		if(object != null){
			builder.withEmbed(object);
		}
		
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
		
		if(object == null){
			return null;
		}
		
		final IMessage[] message = new IMessage[ 1 ];
		IChannel finalChannel = channel;
		RequestBuffer.request(() -> {
			try {
				if(finalChannel == null || object == null) return;
				
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
	
	public static  IMessage sendEmbed( IChannel chat, Color c, String message )
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.withDescription(message);
		builder.withColor(c);
		
		return sendMessage(chat, builder.build());
	}
	
	public static  IMessage sendEmbed( IChannel chat, IUser author, IGuild guild, String message )
	{
		if(guild != null && author != null){
			return sendEmbed(chat, author.getColorForGuild(guild), message);
		}
		
		return sendEmbed(chat, message);
	}
	
	public static  IMessage sendEmbed( IChannel chat,  String message )
	{
		return sendEmbed(chat, null, message);
	}
	
	public static IVoiceChannel getConnectedBotChannel( IGuild guild )
	{
		for (IVoiceChannel channel : guild.getClient().getConnectedVoiceChannels()) {
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
	
	public static String getList( String[] titles, List<String[]> values ){
		CopyOnWriteArrayList<String[]> writeList = new CopyOnWriteArrayList<>();
		writeList.addAll(values);
		
		Integer[] longest_value = new Integer[titles.length];
		int longestLine = -1;
		
		
		for(String[] ob : writeList){
			if(ob.length != titles.length){
				writeList.remove(ob);
				continue;
			}
			
			for(int i = 0; i < ob.length; i++){
				if(longest_value[i] == null || ob[i].length() > longest_value[i]){
					longest_value[i] = ob[i].length();
				}
			}
		}
		
		for(int i = 0; i < titles.length; i++){
			while(longest_value[i] < titles[i].length()){
				longest_value[i] += 1;
			}
		}
		
		String split = "║", cross = "╬", line = "═";
		
		ArrayList<String> strings = new ArrayList<>();
		
		for (String[] object : values) {
			StringBuilder text = new StringBuilder();
			
			for(int i = 0; i < object.length; i++) {
				text.append(object[ i ]);
				
				if(i < (object.length - 1)) {
					text.append(Strings.repeat(" ", longest_value[ i ] - object[ i ].length())).append(" " + split + " ");
				}
			}
			strings.add(text.toString());
		}
		
		for(String t : strings){
			if(t.length() > longestLine){
				longestLine = t.length();
			}
		}
		
		StringBuilder title = new StringBuilder();
		for(int i = 0; i < titles.length; i++){
			title.append(titles[ i ]);
			
			if(i < (titles.length - 1)) {
				title.append(Strings.repeat(" ", longest_value[ i ] - titles[ i ].length())).append(" " + split + " ");
			}
		}
		
		StringBuilder builder1 = new StringBuilder();
		builder1.append(title).append("\n");
		
		if(title.length() > 0) {
			for (int i = 0; i < (longestLine > 0 ? longestLine > title.length() ? longestLine : title.length() : title.length()); i++) {
				boolean isCross = title.length() > i && title.charAt(i) == split.toCharArray()[0];
				builder1.append(isCross ? cross : line);
			}
		}
		builder1.append("\n");
		
		
		for (String t : strings) {
			builder1.append(t).append("\n");
		}
		
		return builder1.toString();
	}
}


