package DiscordBotCore.CommandFiles.Commands.DevCommands;

import DiscordBotCore.DeveloperSystem.DevCommandBase;
import DiscordBotCore.Main.ChatUtils;
import DiscordBotCore.Main.Utils;
import DiscordBotCore.Misc.Annotation.Command;
import DiscordBotCore.Misc.Annotation.Debug;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.Image;

import java.util.List;

@Debug
@Command
public class GenerateEmojiCommand extends DevCommandBase
{
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		String text = String.join(" ", args);
		List<String> urls = Utils.extractUrls(text);
		
		String url = urls.get(0);
		
		for(String t : urls){
			text = text.replace(t, "");
		}
		
		while(text.startsWith(" ") && text.length() > 0){
			text = text.substring(1);
		}
		
		while(text.endsWith(" ") && text.length() > 0){
			text = text.substring(0, text.length() - 1);
		}
		
		IEmoji emoji = generateEmoji(url, text, message.getGuild());
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Created emoji: `" + emoji + "` - " + emoji);
	}
	
	public static IEmoji generateEmoji( String url, String name, IGuild guild){
		String ending = url.substring(url.lastIndexOf(".") + 1);
		Image image = Image.forUrl(ending, url);
		return guild.createEmoji(name, image, new IRole[0]);
	}
	
	@Override
	public String commandPrefix()
	{
		return "genEmoji";
	}
	
	@Override
	public boolean commandPrivateChat()
	{
		return false;
	}
}
