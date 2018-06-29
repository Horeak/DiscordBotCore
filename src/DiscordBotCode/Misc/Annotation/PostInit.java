package DiscordBotCode.Misc.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Excecutes any method annotated with this annotation when the bot is done initializing

@Retention( RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD } )
public @interface PostInit {}
