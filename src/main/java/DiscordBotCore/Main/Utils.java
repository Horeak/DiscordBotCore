package DiscordBotCore.Main;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	public static Random rand = new Random();
	
	public static long getPing( IMessage message){
		Instant time = message.getEditedTimestamp().orElse(message.getCreationDate());
		return Math.abs(System.currentTimeMillis() - time.toEpochMilli());
	}
	
	public static long getWebResponse(){
		long startTime = System.currentTimeMillis();
		
		try {
			java.net.URL u = new URL("https://discordapp.com/");
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			huc.setRequestMethod("GET");
			huc.connect();
		}catch (IOException ignored){ }
		
		return System.currentTimeMillis() - startTime;
	}
	
	public static String limitString(String value, int length)
	{
		if(value == null) return null;
		
		boolean t = false;
		
		while (value.endsWith(" ") && value.length() > 0){
			value = value.substring(0, value.length() - 1);
		}
		
		if(value.length() >= length){
			t = true;
			value = value.substring(0, length - 4);
		}
		
		if(value.endsWith(", ") || value.endsWith(". ")){
			value = value.substring(0, value.length() - 2);
		}
		
		return value + (t && !value.endsWith("..") ? "..." : "");
	}
	
	public static List<String> extractUrls(String text)
	{
		List<String> containedUrls = new ArrayList<String>();
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);
		while (urlMatcher.find()) containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
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
	
	public static Document getDocument( String url) throws IOException
	{
		java.net.URL u = new URL( url);
		HttpURLConnection huc =  (HttpURLConnection)  u.openConnection ();
		huc.setRequestMethod ("GET");
		huc.connect () ;
		int code = huc.getResponseCode();
		
		if(code == 404 || code == 503){
			return null;
		}
		
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		connection.setConnectTimeout(60000);
		connection.setReadTimeout(60000);
		connection.connect();
		
		InputStream stream = connection.getInputStream();
		
		String encoding = connection.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		
		if(stream != null){
			String html = IOUtils.toString(stream, encoding);
			
			if(html != null && !html.isEmpty()) {
				return Jsoup.parseBodyFragment(html);
			}
		}
		
		return null;
	}
}
