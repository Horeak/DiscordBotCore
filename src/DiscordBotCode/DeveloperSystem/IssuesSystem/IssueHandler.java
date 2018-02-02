package DiscordBotCode.DeveloperSystem.IssuesSystem;

import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@SuppressWarnings( "SameParameterValue" )
public class IssueHandler
{
	public static CopyOnWriteArrayList<IssueObject> issueObjects = new CopyOnWriteArrayList<>();
	
	public static File file;
	public static Long timeLimit = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
	
	private static Timer timer = new Timer();
	
	public static void init(){
		file = FileGetter.getFile(DiscordBotBase.FilePath + "/issues.txt");
		load();
		
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run()
			{
				for(IssueObject object : issueObjects){
					if(System.currentTimeMillis() - object.date >= (object.status == EnumIssueStatus.FIXED ? timeLimit / 4 : timeLimit)){
						removeIssue(object);
						removeIssueById(object.id);
					}
				}
			}
		}, 0, TimeUnit.MILLISECONDS.convert(5, TimeUnit.HOURS));
	}
	
	public static void load(){
		try {
			Files.lines(file.toPath()).forEach(( e ) -> {
				IssueObject object = new IssueObject();
				
				try {
					object.loadData(e);
					
					if(object.date > 0){
						if(System.currentTimeMillis() - object.date >= (object.status == EnumIssueStatus.FIXED ? timeLimit / 4 : timeLimit)){
							FileUtil.removeLineFromFile(file, e); //Load issues that has "expired", 30 days for normal issues and about 7 for fixed issues
							return;
						}
					}
					if(object != null && (object.saveData() != null && !object.saveData().isEmpty())) {
						issueObjects.add(object);
					}
					
				}catch (Exception ee){
					if(ee instanceof ArrayIndexOutOfBoundsException) {
						FileUtil.removeLineFromFile(file, e);
						FileUtil.addLineToFile(file, object.saveData());
					}else{
						LoggerUtil.exception(ee);
					}
				}
				

			});
			
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	public static DateFormat formatter = new SimpleDateFormat("EEE MMM d, yyyy, HH:mm zzz", Locale.ENGLISH);
	
	
	public static void createIssue(IssueObject object){
		addIssue(object);
		DevAccess.msgDevs("A new issue has been created! \n```perl\n" + getIssueDisplay(object, false) + "\n```");
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
		for(IssueObject object1 : issueObjects){
			if(isEqual(object, object1)){
				return true;
			}
		}
		
		return false;
	}
	
	public static IssueObject getEqual(IssueObject object){
		for(IssueObject object1 : issueObjects){
			if(isEqual(object, object1)){
				return object1;
			}
		}
		
		return null;
	}
	
	public static boolean isEqual(IssueObject object, IssueObject object1){
		if(object.exceptionIssue && object1.exceptionIssue){
			if((object.exceptionStackTrace.equalsIgnoreCase(object1.exceptionStackTrace) || StringUtils.difference(object.exceptionStackTrace, object1.exceptionStackTrace).length() <= 10) && object.id != object1.id){
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
		
		issueObjects.add(object);
		FileUtil.addLineToFile(file, object.saveData());
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
		if(object == null || !file.exists() || (object != null && object.saveData().isEmpty())){
			return; //Cant remove the issue if object is null, prevent it from loading instead
		}
		
		FileUtil.removeLineFromFile(file, IssueObject.divider + object.id + IssueObject.divider);
		FileUtil.addLineToFile(file, object.saveData());
	}
	
	public static void removeIssue(IssueObject object){
		issueObjects.remove(object);
		FileUtil.removeLineFromFile(file, object.saveData());
	}
	
	public static void removeIssueById(int id){
		if(getIssueById(id) != null){
			removeIssue(getIssueById(id));
		}
		
		FileUtil.removeLineFromFile(file, id + IssueObject.divider);
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
		for(IssueObject object : issueObjects){
			if(object.id == id){
				return object;
			}
		}
		return null;
	}
}
