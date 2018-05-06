package DiscordBotCode.Main;

import java.io.File;
class DLLCleaner
{
	public static final String[] names = new String[]{"libopus", "discord4j", "tempdata", "tempfolder"};
	
	protected static void clean()
	{
		File file = new File(System.getProperty("java.io.tmpdir"));
		for (int i = 0; i < 5; i++) {
			if (file.isDirectory()) {
				for (File e : file.listFiles()) {
					for(String t : names){
						if(e.getName().toLowerCase().contains(t.toLowerCase())){
							e.delete();
						}
					}
				}
			}
		}
	}
}
