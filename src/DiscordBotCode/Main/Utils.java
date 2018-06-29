package DiscordBotCode.Main;

import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	public static Random rand = new Random();
	
	public static long getPing( IMessage message){
		Long t = System.currentTimeMillis() - message.getCreationDate().toEpochMilli();
		
		//I have no idea why this is an issue but with latest D4j it resulted in a negative value at times
		if(t < 0){
			t *= -1;
		}
		return t;
	}
	
	public static List<String> extractUrls(String text)
	{
		List<String> containedUrls = new ArrayList<String>();
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);
		
		int t = 0;
		
		while (urlMatcher.find() && t < 10)
		{
			containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
			t++;
		}
		
		return containedUrls;
	}
	
	public static boolean isInteger( String s )
	{
		return isInteger(s, 10);
	}
	
	public static boolean isInteger( String s, int radix )
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextInt(radix)) {
			return false;
		}
		sc.nextInt(radix);
		return !sc.hasNext();
	}
	
	public static boolean isNumber(String t){
		for(int i = 0; i < t.length(); i++){
			if(!Character.isDigit(t.charAt(i))){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isLong( String s )
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextLong(10)) {
			return false;
		}
		sc.nextLong(10);
		return !sc.hasNext();
	}
	
	public static boolean isBoolean( String s )
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextBoolean()) {
			return false;
		}
		sc.hasNextBoolean();
		return !sc.hasNext();
	}
	
	public static boolean isDouble( String s )
	{
		Scanner sc = new Scanner(s.trim());
		if (!sc.hasNextDouble()) {
			return false;
		}
		sc.hasNextDouble();
		return !sc.hasNext();
	}
	
	
	public static double compareStrings( String stringA, String stringB )
	{
		
		return StringUtils.getLevenshteinDistance(stringA, stringB);
	}
	
	public static String getString(HashMap<String, Object> objectHashMap){
		StringJoiner builder = new StringJoiner(", ", "{", "}");
		
		for(Map.Entry<String, Object> ob : objectHashMap.entrySet()) {
			if (ob != null && ob.getValue() != null && ob.getKey() != null) {
				builder.add(ob.getKey() + "='" + (ob.getValue() != null && ob.getValue().getClass().isArray() ? Arrays.deepToString((Object[]) ob.getValue()) : ob.getValue().toString()) + "'");
			}
		}
		
		return builder.toString().replace("\n", "$_n");
	}
	
	public static HashMap<String, String> getList( String data){
		HashMap<String, String> list = new HashMap<>();
		
		String pattern = "(\\b.*?)(=')(.*?)(')";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(data.replace("\n", "$_n"));
		
		while (m.find()) {
			list.put(m.group(1).replace("$_n", "\n").replace("$_l", "'"), m.group(3).replace("$_n", "\n").replace("$_l", "'"));
		}
		
		return list;
	}
	
	public static ArrayList<HashMap<String, String>> mapFromFile(File fe){
		StringBuilder builder = new StringBuilder();
		
		try {
			Files.lines(fe.toPath()).forEach(( e ) -> {
				if(!e.startsWith("{") && !e.endsWith("}")){
					return;
				}
				builder.append(e).append("\n");
			});
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
		
		String g = builder.toString();
		g = g.replace("\n", "$_n");
		
		String pattern = "(\\{)(.*?)(\\})";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(g);
		
		ArrayList<HashMap<String, String>> list = new ArrayList<>();
		
		while (m.find()) {
			String line = m.group(2).replace("$_n", "\n");
			HashMap<String, String> map = Utils.getList(line);
			list.add(map);
		}
		
		return list;
	}
	
	public static String getMentionString( IMessage message )
	{
		StringJoiner builder = new StringJoiner(" ");
		
		message.getRoleMentions().forEach((r) -> builder.add(r.mention()));
		
		if(!message.mentionsEveryone()) {
			message.getMentions().forEach(( r ) -> builder.add(r.mention()));
		}else{
			builder.add("@everyone");
		}
		
		return builder.toString();
	}
}
