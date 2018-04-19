package DiscordBotCode.DeveloperSystem.IssuesSystem;

public enum EnumIssueStatus
{
	OPEN("⚠"),
	WIP("\uD83D\uDEE0"),
	FIXED("✅");
	
	
	public String icon;
	
	EnumIssueStatus( String icon ) {
		this.icon = icon;
	}
}
