package DiscordBotCode.Misc.Annotation;

import DiscordBotCode.CommandFiles.DiscordChatCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE )
public @interface SubCommand
{
	Class<? extends DiscordChatCommand> parent();
	boolean debug() default false;
}
