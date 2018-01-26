package DiscordBotCode.Main.CommandHandeling.Formatters;

import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.ICommandFormatter;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IgnoredMessageFormat implements ICommandFormatter
{
	@Override
	public IMessage getFormattedMessage( IMessage message )
	{
		String text = message.getContent();
		
		String match = "(?i)(<ignored:)(.*?)(>+)";
		Matcher matcher = Pattern.compile(match).matcher(text);
		
		CopyOnWriteArrayList<IUser> users = new CopyOnWriteArrayList<>(message.getMentions());
		CopyOnWriteArrayList<IRole> roles = new CopyOnWriteArrayList<>(message.getRoleMentions());
		CopyOnWriteArrayList<IChannel> channels = new CopyOnWriteArrayList<>(message.getChannelMentions());
		
		while(matcher.find()){
			String name = ICommandFormatter.getContentBetweenCorresponding(matcher.group().replaceFirst("(?i)(ignored:)", "").replace(" ", ""), '<', '>');
			for(IUser user : users){
				if(user.mention(true).equalsIgnoreCase(name) || user.mention(false).equalsIgnoreCase(name)){
					text = text.replace(matcher.group(), user.mention());
					users.remove(user);
				}
			}
			
			for(IRole role : roles){
				if(role.mention().equalsIgnoreCase(name)){
					text = text.replace(matcher.group(), role.mention());
					roles.remove(role);
				}
			}
			
			for(IChannel channel : channels){
				if(channel.mention().equalsIgnoreCase(name)){
					text = text.replace(matcher.group(), channel.mention());
					channels.remove(channel);
				}
			}
		}
		
		ArrayList<Long> userMentions = new ArrayList<>();
		ArrayList<Long> roleMentions = new ArrayList<>();
		
		if(users.size() > 0) users.forEach((u) -> {
			if(u != null) userMentions.add(u.getLongID());
		});
		if(roles.size() > 0) roles.forEach((u) -> {
			if(u != null) roleMentions.add(u.getLongID());
		});
		
		CommandUtils.setMessageValue(message, "mentions", userMentions);
		CommandUtils.setMessageValue(message, "roleMentions", roleMentions);
		CommandUtils.setMessageValue(message, "channelMentions", channels);
		CommandUtils.setMessageValue(message, "content", text);
		
		return message;
	}
}
