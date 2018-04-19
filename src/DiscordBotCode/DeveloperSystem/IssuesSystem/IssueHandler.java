package DiscordBotCode.DeveloperSystem.IssuesSystem;

import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Config.GsonDataManager;
import DiscordBotCode.Misc.Requests;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IssueHandler
{
	public static class Data{
		public CopyOnWriteArrayList<IssueObject> issueObjects = new CopyOnWriteArrayList<>();
		public ArrayList<Long> postChannels = new ArrayList<>();
	}
	
	public static Long timeLimit = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
	private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
	
	
	//TODO Look into alternative issue repository solutions
	
	private static GsonDataManager<Data> data;
	public static GsonDataManager<Data> data() {
		if (data == null) {
			try {
				data = new GsonDataManager<>(Data.class, DiscordBotBase.FilePath + "/issues.json", Data::new);
			} catch (IOException e) {
				System.err.println("Cannot read from config file?");
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public static void init(){
		load();
		
		timer.scheduleWithFixedDelay(new TimerTask() {
			@Override
			public void run()
			{
				for(IssueObject object : data().get().issueObjects){
					if(System.currentTimeMillis() - object.date >= (object.status == EnumIssueStatus.FIXED ? timeLimit / 4 : timeLimit)){
						removeIssue(object);
						removeIssueById(object.id);
					}
				}
			}
		}, 0, 5, TimeUnit.HOURS);
	}
	
	public static void load(){
		for(IssueObject object : data().get().issueObjects) {
			if(object.date > 0){
				if(System.currentTimeMillis() - object.date >= (object.status == EnumIssueStatus.FIXED ? timeLimit / 4 : timeLimit)){
					data().get().issueObjects.remove(object); //Load issues that has "expired", 30 days for normal issues and about 7 for fixed issues
					return;
				}
			}
		}
		
		try {
			data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	public static DateFormat formatter = new SimpleDateFormat("EEE MMM d, yyyy, HH:mm zzz", Locale.ENGLISH);
	public static DateFormat formatter1 = new SimpleDateFormat("dd/MM/YYYY, HH:mm zzz", Locale.ENGLISH);
	
	
	public static ArrayList<IssueObject> getIssues(){
		ArrayList<IssueObject> objects = new ArrayList<>();
		objects.addAll(IssueHandler.data().get().issueObjects);
		
		return objects;
	}
	
	public static ArrayList<IMessage> getMessages(IssueObject object){
		ArrayList<IMessage> messages = new ArrayList<>();
		
		for(IChannel temp : getChannels()){
			for(Long msId : object.messageIds){
				IMessage ms = temp.fetchMessage(msId);
				
				if(ms != null){
					messages.add(ms);
				}
			}
		}
		
		return messages;
	}
	
	public static ArrayList<IChannel> getChannels(){
		ArrayList<IChannel> channels = new ArrayList<>();
		
		for(Long chId : data().get().postChannels){
			IChannel temp = DiscordBotBase.discordClient.getChannelByID(chId);
			
			if(temp != null){
				channels.add(temp);
			}
		}
		
		return channels;
	}
	
	public static void createIssue(IssueObject object){
		addIssue(object);
	}
	
	public static EmbedObject getEmbed(IssueObject object, IChannel channel){
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.withFooterText("ID: " + object.id + (object.date > 0 ? " | " + "Time: " + formatter1.format(new Date(object.date)) : ""));
		builder.withColor(Color.cyan);
		
		builder.appendField("Status", object.status.icon + " " + object.status.name(), false);
		
		if(object.author != null){
			IUser user = DiscordBotBase.discordClient.fetchUser(object.author);
			
			if(user != null){
				builder.withAuthorName(user.getDisplayName(channel.getGuild()));
				builder.withAuthorIcon(user.getAvatarURL());
			}
		}
		
		if(object.description != null){
			String dg = object.description;
			
			if(dg.length() >= EmbedBuilder.FIELD_CONTENT_LIMIT){
				dg = dg.substring(0, EmbedBuilder.FIELD_CONTENT_LIMIT - 4) + "...";
			}
			
			if(dg != null && !dg.isEmpty()) {
				builder.appendField("Description", dg, false);
			}
		}
		
		if(object.cases > 1){
			builder.appendField("Cases", Integer.toString(object.cases), false);
		}
		
		if(object.exceptionIssue){
			if(object.exceptionStackTrace != null){
				String tg = object.exceptionStackTrace;
				
				String pre = "```perl\n", post = "```";
				int length = pre.length() + post.length();
				
				if(tg.length() >= EmbedBuilder.FIELD_CONTENT_LIMIT){
					tg = tg.substring(0, EmbedBuilder.FIELD_CONTENT_LIMIT - (5 + length)) + "...";
				}
				
				builder.appendField("Stacktrace",  pre + tg + post, false);
			}
		}
		
		
		return builder.build();
	}
	
	public static void sendNewIssueMessage( IssueObject object){
		DevAccess.msgDevs("A new issue has been created! \n```perl\n" + getIssueDisplay(object, false) + "\n```");
		for(IChannel channel : getChannels()){
			
			final IMessage[] message = { null };
			Requests.executeRequestThen(() -> {
					message[ 0 ] = channel.sendMessage(getEmbed(object, channel));
					return message[0] != null;
				},
				() -> {
					object.messageIds.add(message[0].getLongID());
					updateIssue(object);
					return true;
				}, true);
		}
	}
	
	public static String getIssueDisplay(IssueObject object, boolean full){
		String text = "Issue id: " + object.id + "\n";
		
		text += "  > Status: \"" + object.status.name() + "\"";
		if(object.exceptionIssue) text += "\n  > Auto generated issue.";
		if(object.muted) text += "\n  > This issue has been muted.";
		
		
		if(object.date > 0) text += "\n\t> Date: #" + formatter.format(new Date(object.date)) + "\n";
		if(object.exceptionIssue || object.cases > 1) text += "\t> Cases: " + object.cases + "\n";
		if(object.author != null && object.author > -1){
			IUser temp = DiscordBotBase.discordClient.getUserByID(object.author);
			String name;
			if(temp != null){
				name = temp.getName() + "#" + temp.getDiscriminator();
			}else{
				name = "User: " + object.author;
			}
			
			text += "\t> Author: \"" + name + "\"\n";
		}
		
		if(object.description != null && !object.description.isEmpty()) text += "\t  > Desc: \"" + object.description.replace("```", "``") + "\"\n";
		if(object.exceptionIssue) text += "\n\t> Stack trace: " + (!full ? "\"" : "") + (object.exceptionStackTrace != null ? !full ? object.exceptionStackTrace.substring(0, object.exceptionStackTrace.indexOf("\n") - 1) + "..." : object.exceptionStackTrace : "").replace("\n", "\n\t\t\t\t") + (!full ? "\"" : "");
		
		return text;
	}
	
	public static boolean issueExists( IssueObject object){
		for(IssueObject object1 : data().get().issueObjects){
			if(isEqual(object, object1)){
				return true;
			}
		}
		
		return false;
	}
	
	public static IssueObject getEqual(IssueObject object){
		for(IssueObject object1 : data().get().issueObjects){
			if(isEqual(object, object1)){
				return object1;
			}
		}
		
		return null;
	}
	
	public static boolean isEqual(IssueObject object, IssueObject object1){
		if(object.exceptionIssue && object1.exceptionIssue){
			if((object.exceptionStackTrace.equalsIgnoreCase(object1.exceptionStackTrace) || StringUtils.difference(object.exceptionStackTrace, object1.exceptionStackTrace).length() <= 30) && object.id != object1.id){
				return true;
			}
		}
		
		return object1.id == object.id || object1.description != null && Objects.equals(object1.description, object.description) || object1.exceptionStackTrace != null && object1.exceptionStackTrace.equalsIgnoreCase(object.exceptionStackTrace);
	}
	
	public static void addIssue(IssueObject object){
		if(issueExists(object)){
			IssueObject temp = getEqual(object);
			
			getIssueById(temp.id).cases += 1;
			if(temp.status == EnumIssueStatus.FIXED) getIssueById(temp.id).status = EnumIssueStatus.OPEN;
			updateIssue(getIssueById(temp.id));
			
			if(!temp.muted) DevAccess.msgDevs("```perl\nAnother case of issue: " + temp.id + " has occurred\n```");
			return;
		}
		
		sendNewIssueMessage(object);
		data().get().issueObjects.add(object);
		
		try {
			data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	public static void changeStatus( IssueObject ob, EnumIssueStatus status, boolean resetMute){
		ob.status = status;
		
		if(resetMute) ob.muted = false;
		IssueHandler.updateIssue(ob);
		
		notifyIssueAuthor(ob, "```perl\nThe issue you created \"${issue.desc}\" has now been marked as " + status.name() + ".\n```");
	}
	
	public static void notifyIssueAuthor(IssueObject ob, String message){
		if(ob.author != null && !DevAccess.isDev(ob.author)){
			IUser user = DiscordBotBase.discordClient.getUserByID(ob.author);
			
			if(user != null){
				ChatUtils.sendMessage(user.getOrCreatePMChannel(), message.replace("${issue.desc}", ob.description));
			}
		}
	}
	public static void updateIssue(IssueObject object){
		if(object == null){
			return; //Cant remove the issue if object is null, prevent it from loading instead
		}
		
		if(true){
			return;//TODO This is disabled for now
		}
		
		//TODO May need to look into this, it cant fetch messages without the requestbuffer
//		RequestBuffer.request(() -> {
			for(IMessage message : getMessages(object)){
				RequestBuffer.request(() -> message.edit(getEmbed(object, message.getChannel())));
			}
//		});
		
		for(IssueObject ob : data().get().issueObjects){
			if(ob.id == object.id){
				data().get().issueObjects.remove(ob);
			}
		}
		
		data().get().issueObjects.removeIf((i) -> i.id == object.id);
		data().get().issueObjects.add(object);
		
		try {
			data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	public static void removeIssue(IssueObject object){
		RequestBuffer.request(() -> {
			for (IMessage message : getMessages(object)) {
				RequestBuffer.request(message::delete);
			}
		});
		
		data().get().issueObjects.remove(object);
		
		
		try {
			data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	public static void removeIssueById(int id){
		if(getIssueById(id) != null){
			removeIssue(getIssueById(id));
		}
		
		try {
			data().save();
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	
	private static final Random rand = new Random();
	protected static final int minNumber = 100000, maxNumber = 900000;
	
	public static int genId(){
		int n = -1;
		
		while(n == -1 || getIssueById(n) != null){
			n = (int)(minNumber + rand.nextFloat() * maxNumber);
		}
		
		return n;
	}
	
	
	public static IssueObject getIssueById(int id){
		for(IssueObject object : data().get().issueObjects){
			if(object.id == id){
				return object;
			}
		}
		return null;
	}
}
