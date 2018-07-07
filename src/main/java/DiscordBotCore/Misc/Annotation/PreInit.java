package DiscordBotCore.Misc.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Excecutes any method annotated with this annotation before the main bot is initialized

@Retention( RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD } )
public @interface PreInit {}
