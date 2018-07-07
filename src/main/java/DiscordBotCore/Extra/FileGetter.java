package DiscordBotCore.Extra;

import java.io.File;
import java.io.IOException;

@SuppressWarnings( "ResultOfMethodCallIgnored" )
public class FileGetter
{
	public static File getFile( String path )
	{
		File file = new File(path);
		File folder = new File(file.getPath().replace(file.getName(), ""));
		
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return file;
	}
	
	public static File getFolder( String path )
	{
		File file = new File(path);
		
		if (!file.exists() || file.isFile()) {
			file.mkdirs();
			file.mkdir();
		}
		
		return file;
	}
}
