package DiscordBotCode.Main.CommandHandeling.Events;

import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.MessageObject;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class CommandInputEvents
{
	public static final boolean DISPLAY_CHANNEL = true;
	public static final boolean DISPLAY_NAME = true;
	
	public static final int threads_amount = 4;
	
	public final static Object obj = new Object();
	
	public static CopyOnWriteArrayList<handleThreads> threadss = new CopyOnWriteArrayList<>();
	
	public static class messageListener implements IListener<MessageReceivedEvent>
	{
		@Override
		public void handle( MessageReceivedEvent event )
		{
			CommandInputEvents.threadHandle(event.getMessage());
		}
	}
	
	public static class messageEditedListener implements IListener<MessageUpdateEvent>{
		@Override
		public void handle( MessageUpdateEvent event )
		{
			CommandInputEvents.threadHandle(event.getNewMessage());
		}
	}
	
	private static Random rand = new Random();
	public static void threadHandle(IMessage message){
		ArrayList<handleThreads> thWork = new ArrayList<>();
		
		threadss.removeIf((th) -> !th.isAlive());
		
		for(handleThreads th : CommandInputEvents.threadss){
			if(!th.working && !th.isInterrupted() && th.isAlive()){
				thWork.add(th);
			}
		}
		
		if(thWork.size() > 0){
			handleThreads th = thWork.get(rand.nextInt(thWork.size()));
			th.messages.add(new MessageObject(message));
			th.latch.countDown();
		}else {
			handleThreads th = threadss.get(rand.nextInt(threadss.size()));
			th.messages.add(new MessageObject(message));
			th.latch.countDown();
		}
	}
	
	public static void init(){
		for(int i = 0; i < threads_amount; i++){
			handleThreads thread = new handleThreads(i + 1);
			thread.start();
			threadss.add(thread);
		}
	}
	
	public static class handleThreads extends Thread{
		
		public handleThreads(int num)
		{
			this.setName("[Message handling thread][Num:" + num + "]");
		}
		public CopyOnWriteArrayList<MessageObject> messages = new CopyOnWriteArrayList<>();
		
		public MessageObject curMessage;
		public boolean working = false;
		
		protected CountDownLatch latch = new CountDownLatch(1);
		
		
		@Override
		public void run()
		{
			super.run();
			
			while(this.isAlive()) {
				try {
					latch.await();
				} catch (InterruptedException e) {
					DiscordBotBase.handleException(e);
				}
				
				//TODO Fix this shitty handling
				latch = null;
				latch = new CountDownLatch(1);
				
				try {
					for (MessageObject message : messages) {
						curMessage = message;
						working = true;
						
						handle(message);
						
						messages.remove(message);
						curMessage = null;
						working = false;
					}
				} catch (Exception e) {
					DiscordBotBase.handleException(e);
				}
			}
		}
	}
	
	public static void handle( MessageObject message){
		DiscordCommand command = CommandUtils.getDiscordCommand(message.getContent(), message.getChannel());
		String commandName = "";
		
		if(command instanceof DiscordSubCommand){
			if(command != null && ((DiscordSubCommand)command).baseCommand != null) commandName += ((DiscordSubCommand)command).baseCommand.getClass().getSimpleName() + "/";
		}
		
		if(DiscordBotBase.devMode && !DevAccess.isDev(message.getAuthor())) return;
		
		if(command != null) commandName += command.getClass().getSimpleName();
		
		if (!message.getChannel().isPrivate()) {
			if (command != null) {//Only log the message if it was an command
				StringBuilder builder = new StringBuilder();
				
				if(DISPLAY_CHANNEL) builder.append("[").append(message.getGuild().getName()).append("/").append(message.getChannel().getName()).append("]");
				if(DISPLAY_NAME) builder.append("[").append(message.getAuthor().getDisplayName(message.getGuild())).append("]");
				if(DiscordBotBase.debug && !commandName.isEmpty()) builder.append("[").append(commandName).append("]");
				builder.append("Command received: ").append(message.getContent());
				
				System.out.println(builder.toString());
			}
		}
		
		if (message.getChannel().isPrivate()) {
			ChatUtils.incomingPrivateMessageHandle(message);
		} else {
			ChatUtils.incomingMessageHandle(message);
		}
	}
}
