package DiscordBotCode.Main.CommandHandeling.Formatters;

import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.ICommandFormatter;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.Annotation.CommandFormatter;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandFormatter
public class MentionsMessageFormat implements ICommandFormatter
{
	@Override
	public IMessage getFormattedMessage( IMessage message )
	{
		String text = message.getContent();
		
		String match = "(?i)(<mentions:)(.*?)(>+)";
		Matcher matcher = Pattern.compile(match).matcher(text);
		
		CopyOnWriteArrayList<IUser> users = new CopyOnWriteArrayList<>(message.getMentions());
		CopyOnWriteArrayList<IRole> roles = new CopyOnWriteArrayList<>(message.getRoleMentions());
		
		while(matcher.find()){
			String name = ICommandFormatter.getContentBetweenCorresponding(matcher.group().replaceFirst("(?i)(mentions:)", "").replace(" ", ""), '<', '>');
			for(IRole role : roles){
				if( role.mention().equalsIgnoreCase(name) || Utils.isLong(name) && role.getLongID() == Long.parseLong(name)){
					text = text.replace(matcher.group(), "");
					roles.remove(role);
					users.addAll(message.getGuild().getUsersByRole(role));
				}
			}
		}
		
		ArrayList<Long> userMentions = new ArrayList<>();
		ArrayList<Long> roleMentions = new ArrayList<>();
		
		users.forEach((u) -> userMentions.add(u.getLongID()));
		roles.forEach((u) -> roleMentions.add(u.getLongID()));
		
		CommandUtils.setMessageValue(message, "mentions", userMentions);
		CommandUtils.setMessageValue(message, "roleMentions", roleMentions);
		CommandUtils.setMessageValue(message, "content", text);
		
		return message;
	}
}
