package DiscordBotCode.Main.CommandHandeling.Events;

import DiscordBotCode.CommandFiles.CommandBase;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.MessageObject;
import DiscordBotCode.Main.CustomEvents.CommandExecutedEvent;
import DiscordBotCode.Main.CustomEvents.CommandFailedExecuteEvent;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.Annotation.EventListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.concurrent.*;

public class CommandInputEvents
{
	public static final boolean DISPLAY_CHANNEL = true;
	public static final boolean DISPLAY_NAME = true;
	
	public static final int threads_amount = 4;
	
	private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
	
	public static CopyOnWriteArrayList<CommandHandlingThread> threadss = new CopyOnWriteArrayList<>();
	
	@EventListener
	public static void messageEvent( MessageReceivedEvent event )
	{
		CommandInputEvents.threadHandle(event.getMessage());
	}
	
	@EventListener
	public static void messageEdit( MessageEditEvent event ) {
		if(event.getNewMessage() == null && event.getOldMessage() == null){
			System.out.println("Message edit null!");
			return;
		}
		
		if(event.getNewMessage() == null && event.getOldMessage() != null){
			CommandInputEvents.threadHandle(event.getOldMessage());
		}else{
			CommandInputEvents.threadHandle(event.getNewMessage());
		}
	}
	
	private static int lastSelection = 0;
	public static void threadHandle(IMessage message){
		ArrayList<CommandHandlingThread> thWork = new ArrayList<>();
		
		threadss.removeIf((th) -> !th.isAlive());
		
		if(threadss.size() <= 0){
			System.err.println("No available threads for command handling! Handling message directly");
			handle(new MessageObject(message));
			return;
		}
		
		for(CommandHandlingThread th : CommandInputEvents.threadss){
			if(!th.working && !th.isInterrupted() && th.isAlive()){
				thWork.add(th);
			}
		}
		
		if(thWork.size() > 0) {
			if (lastSelection >= thWork.size()) {
				lastSelection = 0;
			}
		}else{
			if(lastSelection >= threadss.size()){
				lastSelection = 0;
			}
		}
		
		if(thWork.size() > 0){
			CommandHandlingThread th = thWork.get(lastSelection);
			th.messages.add(new MessageObject(message));
			th.getLatch().countDown();
			lastSelection++;
			
		}else{
			
			CommandHandlingThread th = threadss.get(lastSelection);
			th.messages.add(new MessageObject(message));
			th.getLatch().countDown();
			lastSelection++;
		}
	}
	
	@EventListener
	public static void commandEvent(CommandExecutedEvent event){
		if(event.getThread() instanceof CommandHandlingThread){
			if(((CommandHandlingThread)event.getThread()).run != null) {
				((CommandHandlingThread) event.getThread()).run.cancel(true);
			}
		}
	}
	
	@EventListener
	public static void commandFailedEvent(CommandFailedExecuteEvent event){
		if(event.getThread() instanceof CommandHandlingThread){
			if(((CommandHandlingThread)event.getThread()).run != null) {
				((CommandHandlingThread) event.getThread()).run.cancel(true);
			}
		}
	}
	
	public static void init(){
		for(int i = 0; i < threads_amount; i++){
			CommandHandlingThread thread = new CommandHandlingThread(i + 1);
			thread.start();
			threadss.add(thread);
		}
	}
	
	
	public static class CommandHandlingThread extends Thread{
		
		public CommandHandlingThread( int num)
		{
			this.setName("[Message handling thread][Num:" + num + "]");
		}
		public CopyOnWriteArrayList<MessageObject> messages = new CopyOnWriteArrayList<>();
		
		public MessageObject curMessage;
		public boolean working = false;
		
		public ScheduledFuture run;
		
		private CountDownLatch latch = new CountDownLatch(1);
		
		public CountDownLatch getLatch(){
			if(latch == null){
				latch = new CountDownLatch(1);
			}
			
			return latch;
		}
		
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
						
						messages.remove(message);//Remove message before handeling so even if there is a issue it doesnt reuse message
						
						try {
							handle(message);
						}catch (Exception e){
							DiscordBotBase.handleException(e);
						}
						
						curMessage = null;
						working = false;
					}
				} catch (Exception e) {
					DiscordBotBase.handleException(e);
				}
			}
		}
	}
	
	public static void startTimer(){
		if(Thread.currentThread() instanceof CommandHandlingThread){
			CommandHandlingThread thread = (CommandHandlingThread)Thread.currentThread();
			
			if(thread.run == null) {
				thread.run = timer.schedule(CommandInputEvents::errorCommand, 45, TimeUnit.SECONDS);
			}
		}
	}
	
	public static void errorCommand(){
		if(Thread.currentThread() instanceof CommandHandlingThread){
			CommandHandlingThread thread = (CommandHandlingThread)Thread.currentThread();
			thread.run = null;
		}
		
		System.err.println("Command was not executed!");
		DiscordBotBase.reconnectBot();
	}
	
	public static void handle( MessageObject message){
		CommandBase command = CommandUtils.getDiscordCommand(message.getContent(), message.getChannel());
		String commandName = "";
		
		if(command instanceof DiscordSubCommand){
			if(command != null && ((DiscordSubCommand)command).baseCommand != null) commandName += ((DiscordSubCommand)command).baseCommand.getClass().getSimpleName() + "/";
		}
		
		if(command != null){
			startTimer();
		}
		
		if(DiscordBotBase.devMode && !DevAccess.isDev(message.getAuthor())) return;
		
		if(command != null) commandName += command.getClass().getSimpleName();
		
		if (command != null) {//Only log the message if it was an command
			if (!message.getChannel().isPrivate()) {
				StringBuilder builder = new StringBuilder();
				
				if(DISPLAY_CHANNEL) builder.append("[").append(message.getGuild().getName()).append("/").append(message.getChannel().getName()).append("]");
				if(DISPLAY_NAME) builder.append("[").append(message.getAuthor().getDisplayName(message.getGuild())).append("]");
				
				if(DiscordBotBase.debug && !commandName.isEmpty()) builder.append("[").append(commandName).append("]");
				builder.append("Command received: ").append(message.getContent().replace("\n", " $\\n "));
				
				System.out.println(builder.toString());
			}else{
				StringBuilder builder = new StringBuilder();
				String prefix = command instanceof DiscordChatCommand ? ((DiscordChatCommand)command).getCommandSign(message.getChannel()) : DiscordBotBase.getCommandSign();
				String commandN = command instanceof DiscordSubCommand ? ((DiscordSubCommand)command).baseCommand.commandPrefix() + " " + command.commandPrefix() : command.commandPrefix();
				
				if(DISPLAY_CHANNEL) builder.append("[").append(message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()).append("]");
				builder.append("Command received in private: ").append(prefix + commandN);
				
				System.out.println(builder.toString());
			}
		}
		
		CommandUtils.executeCommand(message);
	}
}
