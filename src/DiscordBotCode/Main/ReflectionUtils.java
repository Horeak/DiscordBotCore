package DiscordBotCode.Main;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static DiscordBotCode.Main.DiscordBotBase.getReflection;

public class ReflectionUtils
{
	public static void invokeMethods( Class c )
	{
		try {
			List<Method> methods = getMethods(c);
			System.out.println("Found " + methods.size() + " " + c.getSimpleName() + " method" + (methods.size() > 1 ? "s" : "") + "!");
			
			for (Method ob : methods) {
				ob.invoke(null);
			}
		} catch (IllegalAccessException | InvocationTargetException e1) {
			if(e1 instanceof InvocationTargetException){
				InvocationTargetException e2 = (InvocationTargetException)e1;
				
				if(e2 != null && e2.getCause() != null) {
					DiscordBotBase.handleException(e2.getCause());
				}
			}else {
				DiscordBotBase.handleException(e1);
			}
		}
	}
	
	public static <T> List<T> getTypes( Class<T> type, Class c ){
		ArrayList<T> list = new ArrayList<>();
		Set<Class<?>> set1 = getReflection().getTypesAnnotatedWith(c);
	
		for(Class cc : set1){
			try {
				list.add((T) cc.asSubclass(type).newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				DiscordBotBase.handleException(e);
			}
		}
		
		return list;
	}
	
	public static List<Field> getFields( Class c){
		CopyOnWriteArrayList<Field> list = new CopyOnWriteArrayList<>();
		Set set1 = getReflection().getFieldsAnnotatedWith(c);
		list.addAll(set1);
		
		for(Field field : list){
			if(!Modifier.isStatic(field.getModifiers())) {
				System.err.println("Field: " + field + " is not static!");
				list.remove(field);
				continue;
			}
			
			if(!field.isAccessible()){
				field.setAccessible(true);
			}
		}
		
		return list;
	}
	
	public static List<Method> getMethods(Class c){
		return getMethods(c, null);
	}
	
	public static List<Method> getMethods(Class c, Class... parameters){
		CopyOnWriteArrayList<Method> list = new CopyOnWriteArrayList<>();
		Set set1 = getReflection().getMethodsAnnotatedWith(c);
		list.addAll(set1);
		
		for(Method method : list){
			if(!Modifier.isStatic(method.getModifiers())) {
				System.err.println("Method: " + method + " is not static!");
				list.remove(method);
				continue;
			}
			
			if(!method.isAccessible()){
				method.setAccessible(true);
			}
			
			Class[] cc = method.getParameterTypes();
			
			if(parameters != null){
				if(cc.length != parameters.length){
					list.remove(method);
					continue;
				}
				
				for(int i = 0; i < cc.length; i++){
					boolean isSame = false;
					
					if(cc[i] == parameters[i]){
						isSame = true;
					}
					
					if(parameters[i].isAssignableFrom(cc[i]) || cc[i].isAssignableFrom(parameters[i])){
						isSame = true;
					}
					
					if(!isSame){
						list.remove(method);
					}
				}
			}
		}
		
		return list;
	}
}
