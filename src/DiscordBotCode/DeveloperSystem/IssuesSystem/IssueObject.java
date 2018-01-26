package DiscordBotCode.DeveloperSystem.IssuesSystem;

public class IssueObject
{
	
	//TODO Add hide option
	
	public String description;
	public Long author;
	public Long date;
	public int cases = 1;
	public EnumIssueStatus status = EnumIssueStatus.OPEN;
	public int id;
	
	public boolean muted = false; //Added in version 2
	
	public int dataVersion = 2; //Increment version number when saved data was changed
	public int curVersion = 0; //Current version of the object before it is updated by saving
	
	public boolean exceptionIssue;
	public String exceptionStackTrace;
	
	public static String divider = "_DIV_";
	public static String empty = "_EMT_";
	
	
	public static String newLine = "_N_";
	public static String newLine2 = "_N2_";
	public static String tabLine = "_T_";
	
	
	public String saveData(){
		String data = "";
		
		data += dataVersion + divider;
		
		data += id + divider;
		data += (description != null ? description : empty) + divider;
		data += (author != null ? author : empty) + divider;
		data += (date != null ? date : empty) + divider;
		data += (cases) + divider;
		data += (status != null ? status.name() : empty) + divider;
		data += (exceptionIssue) + divider;
		data += (exceptionStackTrace != null && !exceptionStackTrace.isEmpty() ? exceptionStackTrace : empty) + divider;
		
		data += muted + divider; //Added in version 2
		
		return data.replace("\n", newLine).replace("\r", newLine2).replace("\t", tabLine);
	}
	
	public void loadData(String data){
		String temp = data.replace(newLine, "\n").replace(newLine2, "\r").replace(tabLine, "\t");
		String[] text = temp.split(divider);
		
		curVersion = Integer.parseInt(text[0]);
		
		id = Integer.parseInt(text[1]);
		description = text[2].replace(empty, "");
		author = text[3].replace(empty, "").length() > 0 ? Long.parseLong(text[3].replace(empty, "")) : -1;
		date = text[4].replace(empty, "").length() > 0 ? Long.parseLong(text[4].replace(empty, "")) : -1;
		cases = text[5].replace(empty, "").length() > 0 ? Integer.parseInt(text[5].replace(empty, "")) : 1;
		status = EnumIssueStatus.valueOf(text[6].replace(empty, ""));
		exceptionIssue = Boolean.parseBoolean(text[7]);
		exceptionStackTrace = text[8].replace(empty, "");
		
		if(curVersion >= 2) muted = Boolean.parseBoolean(text[9]); //Added in version 2
	}
	
}
