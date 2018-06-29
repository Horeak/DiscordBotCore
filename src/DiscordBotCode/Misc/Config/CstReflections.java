package DiscordBotCode.Misc.Config;

import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Annotation.Debug;
import DiscordBotCode.Misc.Annotation.VariableState;
import com.google.common.collect.Multimap;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
		
		if(!DiscordBotBase.preInit) return;
		
		for(String key : getStore().keySet()){
			Multimap<String, String> ob = getStore().get(key);
			HashMap<String, Collection<String>> map = new HashMap<>(ob.asMap());
			
			ArrayList<String> thc = new ArrayList<>();
			ArrayList<String> removeState = new ArrayList<>();
			
			
			for(Map.Entry<String, Collection<String>> ent : map.entrySet()) {
				if(ent.getKey().equalsIgnoreCase(Debug.class.getName()) && !DiscordBotBase.debug){
					thc.addAll(ent.getValue());
				}
				
				if(ent.getKey().equalsIgnoreCase(VariableState.class.getName())){
					
					for(String t : ent.getValue()){
						try {
							Class c = Class.forName(t);
							
							if(c.isAnnotationPresent(VariableState.class)){
								if(!isVariable((VariableState) c.getAnnotation(VariableState.class))){
									removeState.add(t);
								}
							}
							
						} catch (ClassNotFoundException e) {
							
							int length = t.lastIndexOf(".");
							String g = t.substring(0, length);
							String g1 = t.substring(length + 1);
							
							try {
								Class c1 = Class.forName(g);
								
								//Method
								if(g1.contains("(")){
									String g2 = g1.substring(0, g1.indexOf("("));
									String g3 = g1.substring(g1.indexOf("(") + 1, g1.indexOf(")"));
									ArrayList<Class<?>> classes = new ArrayList<>();
									
									if(g3 == null || g3.isEmpty()) {
										for (String tg : g3.split(",")) {
											if (tg != null && !tg.isEmpty()){
												classes.add(Class.forName(tg));
											}
										}
									}
									
									try {
										Method method = c1.getMethod(g2, classes.size() <= 0 ? null : (Class[])classes.toArray());
										
										if(!isVariable(method.getAnnotation(VariableState.class))){
											removeState.add(t);
										}
										
									} catch (NoSuchMethodException ignored) {}
									
								}else{
									try {
										Field fe = c1.getField(g1);
										if(!isVariable(fe.getAnnotation(VariableState.class))){
											removeState.add(t);
										}
									} catch (NoSuchFieldException ignored) {}
								}
								
							} catch (ClassNotFoundException ignored) {}
						}
					}
				}
			}
			
			for(Map.Entry<String, Collection<String>> ent : map.entrySet()) {
				if(ent.getKey().equalsIgnoreCase(Debug.class.getName())) continue;
				if(ent.getKey().equalsIgnoreCase(VariableState.class.getName())) continue;
				
				for(String t : ent.getValue()){
					if(thc.contains(t) || removeState.contains(t)){
						getStore().get(key).remove(ent.getKey(), t);
					}
				}
			}
		}
	}
	
	public static boolean isVariable(VariableState state){
		if(state != null){
			if(state.variable_class() != null && state.variable_name() != null){
				try {
					Class c = Class.forName(state.variable_class());
					Field fe = c.getDeclaredField(state.variable_name());
					
					if(Modifier.isStatic(fe.getModifiers())) {
						if (fe.getType() == Boolean.class) {
							return state.inverse() ? !(Boolean) fe.get(null) : (Boolean) fe.get(null);
							
						}else if(fe.getType() == boolean.class){
							return state.inverse() != (boolean) fe.get(null);
						}else{
							System.err.println("VariableState variable is not a boolean: " + fe);
						}
					}else{
						System.err.println("VariableState variable is not static: " + fe);
					}
					
				} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
					DiscordBotBase.handleException(e);
				}
			}
		}
		
		return false;
	}
}
