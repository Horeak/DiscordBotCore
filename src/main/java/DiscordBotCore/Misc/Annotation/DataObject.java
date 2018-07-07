package DiscordBotCore.Misc.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( {ElementType.FIELD, ElementType.TYPE} )
public @interface DataObject
{
	String file_path();
	String name() default "";
	boolean use_prefix() default true;
	boolean pretty() default true;
	
}
