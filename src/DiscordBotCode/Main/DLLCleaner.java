package DiscordBotCode.Main;

import java.io.File;

@SuppressWarnings( { "ConstantConditions", "ResultOfMethodCallIgnored" } )
class DLLCleaner
{
	static void clean()
	{
		File file = new File(System.getProperty("java.io.tmpdir"));
		for (int i = 0; i < 20; i++) {
			if (file.isDirectory()) {
				for (File e : file.listFiles()) {
					if (e.getName().toLowerCase().contains("libopus") || e.getName().toLowerCase().contains("discord4j-services") || e.getName().toLowerCase().contains("tempdata") || e.getName().toLowerCase().contains("tempfolder")) {
						e.delete();
					}
				}
			}
		}
	}
}
