package DiscordBotCode.Extra;

import DiscordBotCode.Main.DiscordBotBase;
import org.apache.commons.io.FileUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class FileUtil
{
	public static ConcurrentHashMap<String, Properties> list = new ConcurrentHashMap<>();
	
	public static File getFileFromStream(InputStream stream, String suffix) throws IOException
	{
		File file = File.createTempFile("tempData", suffix);
		FileUtils.copyInputStreamToFile(stream, file);
		return file;
	}
	
	public static File downloadFile(String url, String fileType) throws IOException
	{
		File temp = File.createTempFile("temp", fileType);
		FileUtils.copyURLToFile(new URL(url), temp, 10000, 10000);
		return temp;
	}
	
	public static File downloadFileWithAgent(String url, String fileType) throws IOException
	{
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.connect();
		
		return getFileFromStream(connection.getInputStream(), fileType);
	}
	public static double getImageDifference(BufferedImage img1, BufferedImage img2){
		if(img1 == null || img2 == null){
			System.err.println("Invalid image!");
			return 0;
		}
		
		int width1 = img1.getWidth(null);
		int width2 = img2.getWidth(null);
		int height1 = img1.getHeight(null);
		int height2 = img2.getHeight(null);
		if ((width1 != width2) || (height1 != height2)) {
			System.err.println("Error: Images dimensions mismatch");
			return 0;
		}
		long diff = 0;
		for (int y = 0; y < height1; y++) {
			for (int x = 0; x < width1; x++) {
				int rgb1 = img1.getRGB(x, y);
				int rgb2 = img2.getRGB(x, y);
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >>  8) & 0xff;
				int b1 = (rgb1      ) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >>  8) & 0xff;
				int b2 = (rgb2      ) & 0xff;
				diff += Math.abs(r1 - r2);
				diff += Math.abs(g1 - g2);
				diff += Math.abs(b1 - b2);
			}
		}
		double n = width1 * height1 * 3;
		double p = diff / n / 255.0;
		
		return p;
	}
	
	public static String getValue( String ID, String key )
	{
		if (!list.containsKey(ID)) {
			return null;
		}
		return list.get(ID).getProperty(key);
	}
	
	public static String getValueDefault(String ID, String key, String defaultVal){
		if (!list.containsKey(ID)) {
			return null;
		}
		return list.get(ID).getProperty(key, defaultVal);
	}
	
	public static void initFile( File file, String ID ) throws IOException
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		list.put(ID, prop);
	}
	
	public static boolean removeLineFromFile( File file, String lineRemove )
	{
		try {
			ArrayList<String> strings = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String currentLine;
			
			while ((currentLine = reader.readLine()) != null) {
				String trimmedLine = currentLine.trim();
				if (trimmedLine.toLowerCase().contains(lineRemove.toLowerCase())) {
					continue;
				}
				
				strings.add(currentLine + System.getProperty("line.separator"));
			}
			reader.close();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			for (String t : strings) {
				writer.write(t);
			}
			
			writer.close();
			return true;
			
		} catch (Exception e) {
			DiscordBotBase.handleException(e);
		}
		
		return false;
	}
	
	public static boolean addLineToFile( File file, String lineAdd )
	{
		if(file == null){
			return false;
		}
		
		try {
			ArrayList<String> strings = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				strings.add(currentLine);
			}
			
			strings.add(lineAdd);
			reader.close();
			
			FileUtils.writeLines(file, strings);
			return true;
			
		} catch (Exception e) {
			DiscordBotBase.handleException(e);
		}
		
		return false;
	}
	
	public static float getFileSizeInMB( File file )
	{
		return getMBFromB(getFileSizeInBytes(file));
	}
	
	public static float getMBFromB(float bytes){
		return bytes / (float) (1024 * 1024);
	}
	
	@SuppressWarnings( "WeakerAccess" )
	public static long getFileSizeInBytes( File f )
	{
		long ret = 0;
		if (f.isFile()) {
			return f.length();
		} else if (f.isDirectory()) {
			File[] contents = f.listFiles();
			if (contents != null) {
				for (File content : contents) {
					if (content.isFile()) {
						ret += content.length();
					} else if (content.isDirectory()) {
						ret += getFileSizeInBytes(content);
					}
				}
			}
		}
		return ret;
	}
}

