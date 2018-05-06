package DiscordBotCode.Misc.Config;

import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Annotation.DataLoad;
import DiscordBotCode.Misc.Annotation.DataObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public class DataHandler
{
	private static CopyOnWriteArrayList<Field> objects = new CopyOnWriteArrayList<>();
	private static ConcurrentHashMap<Field, Object> values = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Field, Class> fieldClasses = new ConcurrentHashMap<>();
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	private static final Gson gson1 = new GsonBuilder().serializeNulls().create();
	
	private static String prefix = "";
	
	public static void init(String path){
		prefix = path;
		new SaveDataThread().start();
	}
	
	private static ArrayList<Field> toSave = new ArrayList<>();
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private static class SaveDataThread extends Thread{
		@Override
		public void run()
		{
			super.run();
			
			//TODO Add support for classes annotated with DataObject
			
			Reflections reflections = new Reflections("", new FieldAnnotationsScanner());
			Set<Field> fields = reflections.getFieldsAnnotatedWith(DataObject.class);
			
			for(Field field : fields){
				try {
					load(field);
				} catch (IOException | IllegalAccessException e) {
					DiscordBotBase.handleException(e);
				}
				
				objects.add(field);
				Object t = null;
				
				try {
					t = getValue(field);
				} catch (IllegalAccessException e) {
					DiscordBotBase.handleException(e);
				}
				
				if(t != null) {
					updateValue(field, t);
				}
			}
			
			//TODO Try and find a more optimized way of checking and saving values in near real time
			while(isAlive()){
				try {
					check();
				} catch (IllegalAccessException e) {
					DiscordBotBase.handleException(e);
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					DiscordBotBase.handleException(e);
				}
			}
		}
	}
	
	private static void updateValue(Field fe, Object t){
		Object tg = gson.fromJson(gson.toJson(t), fe.getGenericType());
		values.put(fe, tg); //TODO This is very bad!
	}
	
	private static void check() throws IllegalAccessException
	{
		for(Field fe : objects){
			if(values.containsKey(fe)){
				Object t = getValue(fe);
				Object tg = values.get(fe);
				
				if (t != null && !Objects.equals(t, tg) && !t.equals(tg)) {
					if(!gson1.toJson(t).equals(gson1.toJson(tg))) {
						differentValue(fe, t);
					}
				}
			}
		}
	}
	
	private static void differentValue(Field fe, Object value)
	{
		updateValue(fe, value);
		
		if(!toSave.contains(fe)) {
			toSave.add(fe);
			
			executor.schedule(() -> {
				toSave.remove(fe);
				
				try {
					save(fe);
				} catch (IllegalAccessException e) {
					DiscordBotBase.handleException(e);
				}
			}, 1000, TimeUnit.MILLISECONDS);
		}
	}
	
	
	private static void load(Field fe) throws IOException, IllegalAccessException
	{
		if(fe.isAnnotationPresent(DataObject.class)){
			DataObject ob = fe.getAnnotation(DataObject.class);
			
			File fes = FileGetter.getFile((ob.use_prefix() ? prefix + "/" : "") + ob.file_path());
			
			JsonElement el = gson.fromJson(FileIOUtils.read(fes.toPath()), JsonElement.class);
			String key = !ob.name().isEmpty() ? ob.name() : fe.getDeclaringClass().getName() + "|" + fe.getName();
			
			if(el != null) {
				JsonObject obs = el.getAsJsonObject();
				if (obs.has(key)) {
					JsonElement t1 = obs.get(key);
					Object tg = gson.fromJson(t1, fe.getGenericType());
					
					setValue(fe, tg);
					updateValue(fe, tg);
					
					notifyDataLoad(fe);
				}
			}
		}
	}
	
	private static void notifyDataLoad( Field fe ) throws IllegalAccessException
	{
		for (Method method : fe.getDeclaringClass().getMethods()) {
			if (method.isAnnotationPresent(DataLoad.class)) {
				Class[] tt = method.getParameterTypes();
				
				if (tt != null && tt.length == 1) {
					if (tt[ 0 ] == Field.class) {
						try {
							method.invoke(fe, fe);
						} catch (InvocationTargetException e) {
							DiscordBotBase.handleException(e);
						}
					}
				}else if(tt.length <= 0){
					try {
						method.invoke(fe);
					} catch (InvocationTargetException e) {
						DiscordBotBase.handleException(e);
					}
				}
			}
		}
	}
	
	private static void save(Field fe) throws IllegalAccessException
	{
		HashMap<String, Object> saveData = new HashMap<>();
		
		ArrayList<Field> obs = new ArrayList<>();
		DataObject ob = fe.getAnnotation(DataObject.class);
		String key = !ob.name().isEmpty() ? ob.name() : fe.getDeclaringClass().getName() + "|" + fe.getName();
		
		File fes = FileGetter.getFile((ob.use_prefix() ? prefix + "/" : "") + ob.file_path());
		obs.add(fe);
		
		saveData.put(key, values.containsValue(fe) ? values.get(fe) : getValue(fe));
		
		for(Field field : objects){
			if(field.isAnnotationPresent(DataObject.class)){
				DataObject ob1 = field.getAnnotation(DataObject.class);
				if(ob1.file_path().equals(ob.file_path())){
					if(!obs.contains(field)){
						obs.add(field);
						saveData.put(!ob1.name().isEmpty() ? ob1.name() : (field.getDeclaringClass().getName() + "|" + field.getName()), values.containsValue(field) ? values.get(field) : getValue(field));
					}
				}
			}
		}
		
		try {
			FileIOUtils.write(fes.toPath(), gson.toJson(saveData));
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
	}
	
	private static Object getValue(Field field) throws IllegalAccessException
	{
		if(Modifier.isStatic(field.getModifiers())) {
			if(!field.isAccessible()){
				field.setAccessible(true);
			}
			
			if(fieldClasses.containsKey(field)){
				return field.get(fieldClasses.get(field));
			}else {
				Class t = field.getDeclaringClass();
				fieldClasses.put(field, t);
				
				return field.get(t);
			}
		}
		
		return null;
	}
	
	private static void setValue(Field field, Object value) throws IllegalAccessException
	{
		if(Modifier.isStatic(field.getModifiers())) {
			if(!field.isAccessible()){
				field.setAccessible(true);
			}
			
			Class t = null;
			
			if(fieldClasses.containsKey(field)){
				t = fieldClasses.get(field);
			}else {
				t = field.getDeclaringClass();
				fieldClasses.put(field, t);
			}
			
			if(t != null){
				field.set(t, value);
			}
		}
	}
}
