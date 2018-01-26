package DiscordBotCode.DeveloperSystem.IssuesSystem;

import org.apache.commons.lang.exception.ExceptionUtils;
import sx.blah.discord.handle.obj.IUser;

public class IssueBuilder
{
	private IssueObject object = new IssueObject();
	
	
	public IssueBuilder withDescription( String desc){
		object.description = desc;
		return this;
	}
	
	public IssueBuilder withAuthor(Long id){
		object.author = id;
		return this;
	}
	
	public IssueBuilder withAuthor( IUser user ){
		object.author = user.getLongID();
		return this;
	}
	
	public IssueBuilder withDate(Long date){
		object.date = date;
		return this;
	}
	
	public IssueBuilder withCurrentDate(){
		object.date = System.currentTimeMillis();
		return this;
	}
	
	public IssueBuilder withException(Exception e){
		object.exceptionStackTrace = ExceptionUtils.getStackTrace(e);
		object.exceptionIssue = true;
		return this;
	}
	
	public IssueBuilder withException(String e){
		object.exceptionStackTrace = e;
		object.exceptionIssue = true;
		return this;
	}
	
	public IssueBuilder genId(){
		object.id = IssueHandler.genId();
		return this;
	}
	
	public IssueObject build(){
		return object;
	}
	
}
