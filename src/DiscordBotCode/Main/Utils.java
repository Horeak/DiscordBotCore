package DiscordBotCode.Main;

import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	public static Random rand = new Random();
	
	public static long getPing( IMessage message){
		Date date = Date.from(message.getCreationDate().atZone(ZoneId.systemDefault()).toInstant());
		Date curDate = new Date();
		return curDate.getTime() - date.getTime();
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
	
	public static void reportMemoryUsage()
	{
		Runtime runtime = Runtime.getRuntime();
		int mb = 1024 * 1024;
		
		System.out.println("######################################");
		System.out.println("Used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / mb + "mb");
		System.out.println("Free Memory:" + runtime.freeMemory() / mb + "mb");
		System.out.println("Total Memory:" + runtime.totalMemory() / mb + "mb");
		System.out.println("Max Memory:" + runtime.maxMemory() / mb + "mb");
		System.out.println("######################################");
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
}
