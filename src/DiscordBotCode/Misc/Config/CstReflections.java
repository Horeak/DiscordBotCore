package DiscordBotCode.Misc.Config;

import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Annotation.Debug;
import com.google.common.collect.Multimap;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//Custom reflection class to add filters to the reflection search
public class CstReflections extends Reflections
{
	public CstReflections( Configuration configuration )
	{
		super(configuration);
	}
	
	public CstReflections( String prefix, Scanner... scanners )
	{
		super(prefix, scanners);
	}
	
	public CstReflections( Object... params )
	{
		super(params);
	}
	
	protected void scan() {
		super.scan();
		
		if(DiscordBotBase.debug) return;
		
		for(String key : getStore().keySet()){
			Multimap<String, String> ob = getStore().get(key);
			HashMap<String, Collection<String>> map = new HashMap<>(ob.asMap());
			
			ArrayList<String> thc = new ArrayList<>();
			
			for(Map.Entry<String, Collection<String>> ent : map.entrySet()) {
				if(ent.getKey().equalsIgnoreCase(Debug.class.getName())){
					thc.addAll(ent.getValue());
				}
			}
			
			for(Map.Entry<String, Collection<String>> ent : map.entrySet()) {
				if(ent.getKey().equalsIgnoreCase(Debug.class.getName())) continue;
				
				for(String t : ent.getValue()){
					if(thc.contains(t)){
						getStore().get(key).remove(ent.getKey(), t);
					}
				}
			}
		}
	}
}
