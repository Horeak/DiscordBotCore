package DiscordBotCode.Main.CommandHandeling.Formatters;

import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.ICommandFormatter;
import DiscordBotCode.Main.CommandHandeling.ObjectConverter;
import DiscordBotCode.Misc.Annotation.CommandFormatter;
import sx.blah.discord.handle.obj.IMessage;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandFormatter
public class CommandDataFormat implements ICommandFormatter
{
	@Override
	public IMessage getFormattedMessage( IMessage message )
	{
		if(!DevAccess.isDev(message.getAuthor())){
			return message;
		}
		
		String text = message.getContent();
		
		String match = "(?i)(-data\\[)(.*?)(\\]$)";
		Matcher matcher = Pattern.compile(match).matcher(text);
		boolean hasText = false;
		
		while(matcher.find()){
			String tex = ICommandFormatter.getContentBetweenCorresponding(matcher.group(), '[', ']');
			
			if(tex != null) {
				String[] tt = tex.split(", ");
				for (String tg : tt) {
					String[] t = tg.split("=");
					Field field = CommandUtils.getField(t[0]);
					
					if(field != null){
						if(field.getName().equals("content")){
							hasText = true;
						}
						
						Object tk = ObjectConverter.convert(t[1], field.getType());
						CommandUtils.setMessageValue(message, field.getName(), tk);
					}else{
						ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no data field with name \"" + t[0] + "\"");
					}
				}
			}
			
			text = text.replace(matcher.group(), "");
		}
		
		if(!hasText) CommandUtils.setMessageValue(message, "content", text);
		
		return message;
	}
}
