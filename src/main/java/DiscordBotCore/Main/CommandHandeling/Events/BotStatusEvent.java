package DiscordBotCore.Main.CommandHandeling.Events;

import DiscordBotCore.Extra.FileUtil;
import DiscordBotCore.Main.CustomEvents.BotCloseEvent;
import DiscordBotCore.Main.DiscordBotBase;
import DiscordBotCore.Misc.Annotation.DataObject;
import DiscordBotCore.Misc.Annotation.EventListener;
import DiscordBotCore.Misc.Annotation.PostInit;
import DiscordBotCore.Misc.TimeParserUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;

import java.io.IOException;
import java.util.Date;

public class BotStatusEvent
{
	@DataObject( file_path = "botStatus.json", name = "lastOnline")
	private static Date lastOnline = null;
	
	private static String WEBHOOK = null;
	private static boolean lastState = false;
	
	@EventListener
	public static void disconnectEvent( DisconnectedEvent event ){
		if(event.getReason() == DisconnectedEvent.Reason.ABNORMAL_CLOSE) return; //This generates alot of spam if left on
		
		try {
			post(getString(false, "Reason: " + event.getReason()));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}

	@EventListener
	public static void connectedEvent( ReconnectSuccessEvent event ){
		try {
			post(getString(true));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	@EventListener
	public static void connectedEvent( LoginEvent event ){
		try {
			post(getString(true));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	@EventListener
	public static void connectedEvent( ReadyEvent event ){
		try {
			post(getString(true));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	@EventListener
	public static void botClose( BotCloseEvent event ){
		try {
			post(getString(false));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	@PostInit
	public static void postInit(){
		try {
			post(getString(true));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	
	private static String genJson( String content, String color, String footer ){
		return "{\"username\": \"" + DiscordBotBase.discordClient.getOurUser().getName() + "\",\"content\":\"@here\","
		       + "\"avatar_url\":\"" + DiscordBotBase.discordClient.getOurUser().getAvatarURL() + "\","
		       + "\"embeds\":[{\"author\":{ \"name\":" + "\"" + DiscordBotBase.discordClient.getOurUser().getName() + "\","
		       + "\"icon_url\":" + "\"" + DiscordBotBase.discordClient.getOurUser().getAvatarURL() + "\"},"
		       + (footer != null && !footer.isEmpty() ? "\"footer\":{\"text\":\"" + footer + "\"}," : "")
		       + "\"description\":\"" + content + "\", \"color\":" + color + "}]}";
		
	}
	
	private static String getString(boolean status){
		return getString(status, null);
	}
	
	
	private static String getString(boolean status, String footer1){
		if(WEBHOOK == null){
			WEBHOOK = FileUtil.getValue(DiscordBotBase.INFO_FILE_TAG, "status_webhook");
		}
		
		if(lastState == status) return null;
		lastState = status;
		
		
		String footer = null;
		if(footer1 == null) {
			if (lastOnline != null && status) {
				String text = TimeParserUtil.getTimeText(lastOnline);
				
				if (text != null && !text.isEmpty()) {
					footer = "Offline for: " + text;
					lastOnline = null;
				}
			}
		}else{
			footer = footer1;
		}
		
		if(!status && lastOnline == null) 	lastOnline = new Date();
		
		return genJson((status ? "Bot is now online!" : "Bot is now offline!"), (status ? "65280" : "16711680"), footer);
	}
	
	private static HttpResponse post(String json) throws IOException
	{
		if(json == null || json.isEmpty()) return null;
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(WEBHOOK);
		post.addHeader("Content-Type", "application/json");
		post.setEntity(new StringEntity(json));
		
		return httpClient.execute(post);
	}
	
}
