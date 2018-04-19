package DiscordBotCode.DeveloperSystem;

import DiscordBotCode.Main.ChatUtils;
import sx.blah.discord.handle.obj.IMessage;

public class DevNotifications extends DevCommandBase
{
	@Override
	public String commandPrefix()
	{
		return "devNotif";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(DevAccess.isDev(message.getAuthor())){
			DevUserObject t = null;
			
			if(DevAccess.getDevData(message.getAuthor()) != null){
				t = DevAccess.getDevData(message.getAuthor());
			}
			
			
			if(t != null) {
				t.notifications = !t.notifications;
				DevAccess.updateDevData(message.getAuthor(), t);
			}
		}else{
			return;
		}
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " You have now " + (DevAccess.getDevData(message.getAuthor()) != null ? (DevAccess.getDevData(message.getAuthor()).notifications ? "enabled" : "disabled") : "Null!") + " DevData notifications!");
	}
}
