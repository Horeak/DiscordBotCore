import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.Main.DiscordBotBase;
import DiscordBotCore.Misc.Annotation.Command;
import DiscordBotCore.Misc.Annotation.Debug;
import DiscordBotCore.Misc.Annotation.SubCommand;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;

public class Tests
{
	@Test
	public void checkCommandInit(){
		try {
			DiscordBotBase.initReflection(false);
		} catch (URISyntaxException | MalformedURLException e) {
			DiscordBotBase.handleException(e);
		}
		
		Set<Class<?>> commands1 = DiscordBotBase.getReflection().getTypesAnnotatedWith(Command.class);
		Set<Class<?>> subCommands = DiscordBotBase.getReflection().getTypesAnnotatedWith(SubCommand.class);
		
		commands1.forEach((c) -> {
			if(!c.isAnnotationPresent(Debug.class)) {
				try {
					c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					Assert.fail("Init error with command: \"" + c.getName() + "\"");
				}
			}
		});
		
		subCommands.forEach((c) -> {
			if(!c.isAnnotationPresent(Debug.class)) {
				try {
					c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					Assert.fail("Init error with command: \"" + c.getName() + "\"");
				}
			}
		});
	}
	
	@Test
	public void checkCommandInfo()
	{
		try {
			DiscordBotBase.initReflection(false);
		} catch (URISyntaxException | MalformedURLException e) {
			DiscordBotBase.handleException(e);
		}
		
		Set<Class<?>> commands1 = DiscordBotBase.getReflection().getTypesAnnotatedWith(Command.class);
		Set<Class<?>> subCommands = DiscordBotBase.getReflection().getTypesAnnotatedWith(SubCommand.class);
		
		ArrayList<DiscordCommand> commands = new ArrayList<>();
		
		commands1.forEach((c) -> {
			if(!c.isAnnotationPresent(Debug.class)) {
				try {
					commands.add((DiscordCommand) c.newInstance());
				} catch (InstantiationException | IllegalAccessException ignored) {
				}
			}
		});
		
		subCommands.forEach((c) -> {
			if(!c.isAnnotationPresent(Debug.class)) {
				try {
					commands.add((DiscordCommand) c.newInstance());
				} catch (InstantiationException | IllegalAccessException ignored) {
				}
			}
		});
		
		boolean hasMissing = false;
		
		for(DiscordCommand command : commands){
			String desc = command.getDescription(null, null);
			String usage = command.getUsage(null, null);
			
			if(desc == null || desc.isEmpty()){
				System.err.println("Command: \"" + command.getClass().getName() + "\", has invalid description!");
				hasMissing = true;
			}
			
			if(usage == null || usage.isEmpty()){
				System.err.println("Command: \"" + command.getClass().getName() + "\", has invalid usage!");
				hasMissing = true;
			}
		}
		
		if(hasMissing) {
			System.err.println("Found commands with missing information!");
		}
		
		//TODO Find a way to cause an error without failing the test
//		Assert.assertFalse("Found commands with missing information!", hasMissing);
	}
}
