package DiscordBotCode.DeveloperSystem.IssuesSystem;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class IssueCommand extends DiscordChatCommand
{
	public IssueCommand()
	{
		subCommands.add(new create(this));
		subCommands.add(new close(this));
		subCommands.add(new wip(this));
		subCommands.add(new delete(this));
		subCommands.add(new open(this));
		subCommands.add(new mute(this));
		
		IssueHandler.init();
	}
	
	@Override
	public String commandPrefix()
	{
		return "Issue";
	}
	
	@Override
	public boolean listCommand()
	{
		return false;
	}
	
	protected int perPage = 5;
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		CopyOnWriteArrayList<IssueObject> issues = new CopyOnWriteArrayList<>();
		issues.addAll(IssueHandler.issueObjects);
		
		//Sorting
		/*
		  Player generated issues which are still OPEN have highest priority
		  Auto generated issues with OPEN are second highest and are sorted by amount of cases from most to least
		  After that it is WIP and CLOSED in that order, within each group each issue is sorted by date
		  Muted issues are sorted as last
		 */
		issues.sort(( o1, o2 ) -> {
			if(o1.muted && !o2.muted || !o1.muted && o2.muted){
				if(o1.muted){
					return Integer.compare(1, 0);
				}else if(o2.muted){
					return Integer.compare(0, 1);
				}
			}
			
			if(o1.status == o2.status){
				if(o1.exceptionIssue && !o2.exceptionIssue || !o1.exceptionIssue && o2.exceptionIssue){
					return Boolean.compare(o1.exceptionIssue, o2.exceptionIssue);
				}
				
				if(o1.exceptionIssue && o2.exceptionIssue){
					if(o1.cases == o2.cases){
						return Long.compare(o2.date, o1.date);
					}else {
						return Integer.compare(o2.cases, o1.cases);
					}
				}
				return Long.compare(o2.date, o1.date);
			}
			
			return Integer.compare(o1.status.ordinal(), o2.status.ordinal());
		});
		
		if(DevAccess.isDev(message.getAuthor())){
			int page = 1;
			int pages;
			
			ArrayList<Integer> ids = new ArrayList<>();
			
			for(String t : args){
				if(Utils.isInteger(t)){
					int tg = Integer.parseInt(t);
					
					if(tg < IssueHandler.minNumber){
						page = tg;
					}else{
						ids.add(tg);
					}
				}
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append("```perl\n");
			
			if(ids.size() > 0){
				pages = (int)Math.nextUp(ids.size() / perPage);
				if(pages * perPage < ids.size()){
					pages += 1;
				}
			}else {
				pages = (int)Math.nextUp(issues.size() / perPage);
				if(pages * perPage < issues.size()){
					pages += 1;
				}
			}
			
			if(page > pages){
				page = pages;
			}
			
			ArrayList<StringBuilder> builders = new ArrayList<>();
			boolean multi = false;
			
			if(page > 0 && (ids.size() > 0 || issues.size() > 0)) {
				for (int i = ((page - 1) * perPage); i < (page * perPage); i++) {
					if (ids.size() > 0) {
						multi = true;
						
						if (i < ids.size()) {
							IssueObject object = IssueHandler.getIssueById(ids.get(i));
							
							if (object != null) {
								builder.append("[").append(i + 1).append("] ").append(IssueHandler.getIssueDisplay(object, (ids.size() < perPage))).append("\n\n");
							}
							
						}
					} else {
						if (i < issues.size()) {
							IssueObject object = issues.get(i);
							
							if (object != null) {
								builder.append("[").append(i + 1).append("] ").append(IssueHandler.getIssueDisplay(object, false)).append("\n\n");
							}
						}
					}
				}
				
				if(builder.toString().length() >= 1900){
					multi = true;
					builder.append("\nPage: [ " + page + " / " + pages + " ]");
				}
				
				if (multi) {
					int cur = 0;
					int max = 1800;
					
					builders.add(new StringBuilder());
					
					String[] g = builder.toString().split("\n");
					
					for (String t : g) {
						builders.get(cur).append(t).append("\n");
						
						if (builders.get(cur).length() >= max) {
							builders.add(new StringBuilder());
							cur += 1;
							builders.get(cur).append("```perl\n");
						}
					}
				}
				
				if (multi) {
					int i = 0;
					for (StringBuilder builder1 : builders) {
						i += 1;
						
						if(builder1.toString().length() > 5) {
							ChatUtils.sendMessage(message.getChannel(), (i == 1 ? message.getAuthor().mention() : "") + builder1.toString() + "```");
						}
					}
				} else {
					if(builder.toString().length() > 30) {
						ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + builder.toString() + "Page: [ " + page + " / " + pages + " ]" + "```");
					}else{
						ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no issues!");
					}
				}
			}else{
				ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no issues!");
			}
		}else{
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Please use the \"create\" sub-command if you wish to submit a issue!");
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}

class create extends DiscordSubCommand
{
	public create( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "create";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		IssueHandler.createIssue(new IssueBuilder().withAuthor(message.getAuthor()).withCurrentDate().genId().withDescription(String.join(" ", args)).build());
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Issue has been created!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}

class close extends DiscordSubCommand{
	public close( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "close";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<IssueObject> objects = new ArrayList<>();
		
		for(String t : args){
			if(t.equalsIgnoreCase("all")){
				objects.addAll(IssueHandler.issueObjects);
				break;
			}
			
			if(Utils.isInteger(t)){
				int tg = Integer.parseInt(t);
				
				IssueObject temp = IssueHandler.getIssueById(tg);
				
				if(temp != null){
					objects.add(temp);
				}
			}
		}
		
		for(IssueObject ob : objects){
			IssueHandler.changeStatus(ob, EnumIssueStatus.FIXED, false);
			if(objects.size() <= 3) DevAccess.msgDevs("```perl\nIssue: " + ob.id + "\n> Has been marked as FIXED by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		if(objects.size() > 3){
			DevAccess.msgDevs("```perl\n" + objects.size() + " Issues as been marked as FIXED by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The specific issues have been updated!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean hasPermissions( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor());
	}
}

class wip extends DiscordSubCommand{
	public wip( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "wip";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<IssueObject> objects = new ArrayList<>();
		
		for(String t : args){
			if(t.equalsIgnoreCase("all")){
				objects.addAll(IssueHandler.issueObjects);
				break;
			}
			
			if(Utils.isInteger(t)){
				int tg = Integer.parseInt(t);
				
				IssueObject temp = IssueHandler.getIssueById(tg);
				
				if(temp != null){
					objects.add(temp);
				}
			}
		}
		
		for(IssueObject ob : objects){
			IssueHandler.changeStatus(ob, EnumIssueStatus.WIP, true);
			if(objects.size() <= 3)DevAccess.msgDevs("```perl\nIssue: " + ob.id + "\n> Has been marked as WIP by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		if(objects.size() > 3){
			DevAccess.msgDevs("```perl\n" + objects.size() + " Issues as been marked as WIP by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The specific issues have been updated!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean hasPermissions( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor());
	}
}

class delete extends DiscordSubCommand{
	public delete( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "delete";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		for(String t : args){
			if(t.equalsIgnoreCase("all")){
				for(IssueObject ob : IssueHandler.issueObjects){
					if(IssueHandler.issueObjects.size() <= 3) DevAccess.msgDevs("```perl\nIssue: " + ob + "\n> Has been deleted by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
					IssueHandler.removeIssue(ob);
					IssueHandler.removeIssueById(ob.id);
				}
				
				if(IssueHandler.issueObjects.size() > 3){
					DevAccess.msgDevs("```perl\n" + IssueHandler.issueObjects.size() + " Issues as been marked deleted by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
				}
				break;
			}
			if(Utils.isInteger(t)){
				int tg = Integer.parseInt(t);
				IssueHandler.removeIssueById(tg);
				DevAccess.msgDevs("```perl\nIssue: " + tg + "\n> Has been deleted by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
			}
		}
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The specific issues have been deleted!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean hasPermissions( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor());
	}
}

class open extends DiscordSubCommand{
	public open( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "open";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<IssueObject> objects = new ArrayList<>();
		
		for(String t : args){
			if(t.equalsIgnoreCase("all")){
				objects.addAll(IssueHandler.issueObjects);
				break;
			}
			
			if(Utils.isInteger(t)){
				int tg = Integer.parseInt(t);
				
				IssueObject temp = IssueHandler.getIssueById(tg);
				
				if(temp != null){
					objects.add(temp);
				}
			}
		}
		
		for(IssueObject ob : objects){
			IssueHandler.changeStatus(ob, EnumIssueStatus.OPEN, true);
			if(objects.size() <= 3)DevAccess.msgDevs("```perl\nIssue: " + ob.id + "\n> Has been marked as OPEN by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		if(objects.size() > 3){
			DevAccess.msgDevs("```perl\n" + objects.size() + " Issues as been marked as OPEN by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The specific issues have been updated!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean hasPermissions( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor());
	}
}

class mute extends DiscordSubCommand{
	public mute( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "mute";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<IssueObject> objects = new ArrayList<>();
		
		for(String t : args){
			if(t.equalsIgnoreCase("all")){
				objects.addAll(IssueHandler.issueObjects);
				break;
			}
			
			if(Utils.isInteger(t)){
				int tg = Integer.parseInt(t);
				
				IssueObject temp = IssueHandler.getIssueById(tg);
				
				if(temp != null){
					objects.add(temp);
				}
			}
		}
		
		for(IssueObject ob : objects){
			ob.muted = true;
			IssueHandler.updateIssue(ob);
			
			if(objects.size() <= 3)DevAccess.msgDevs("```perl\nIssue: " + ob.id + "\n> Has been muted by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		
		if(objects.size() > 3){
			DevAccess.msgDevs("```perl\n" + objects.size() + " Issues as been muted by: \"" + (message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()) + "\"\n```", message.getAuthor());
		}
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " The specific issues have been updated!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean hasPermissions( IMessage message, String[] args )
	{
		return DevAccess.isDev(message.getAuthor());
	}
}

