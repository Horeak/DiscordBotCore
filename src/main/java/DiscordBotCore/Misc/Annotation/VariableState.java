package DiscordBotCore.Misc.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Check the value of a specific boolean in any class, the value of the boolean will not update after preInit so use @PreInit to set the value

@Retention( RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.TYPE, ElementType.METHOD } )
public @interface VariableState
{
	String variable_class();
	String variable_name();
	boolean inverse() default false;
}
