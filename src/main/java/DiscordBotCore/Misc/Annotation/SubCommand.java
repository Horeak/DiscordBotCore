package DiscordBotCore.Misc.Annotation;

import DiscordBotCore.CommandFiles.DiscordCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE )
public @interface SubCommand
{
	Class<? extends DiscordCommand> parent();
}
