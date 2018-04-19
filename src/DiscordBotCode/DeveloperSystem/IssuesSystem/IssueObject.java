package DiscordBotCode.DeveloperSystem.IssuesSystem;

import java.util.ArrayList;

public class IssueObject
{
	public String description;
	public Long author;
	public Long date;
	public int cases = 1;
	public EnumIssueStatus status = EnumIssueStatus.OPEN;
	public int id;
	
	public boolean muted = false; //Added in version 2

	public boolean exceptionIssue;
	public String exceptionStackTrace;
	
	public ArrayList<Long> messageIds = new ArrayList<>();
}
