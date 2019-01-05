package DiscordBotCore.Main.CommandHandeling.Events;

import DiscordBotCore.CommandFiles.DiscordCommand;
import DiscordBotCore.DeveloperSystem.DevAccess;
import DiscordBotCore.Main.CommandHandeling.CommandUtils;
import DiscordBotCore.Main.CommandHandeling.MessageObject;
import DiscordBotCore.Main.CustomEvents.CommandExecutedEvent;
import DiscordBotCore.Main.CustomEvents.CommandFailedExecuteEvent;
import DiscordBotCore.Main.DiscordBotBase;
import DiscordBotCore.Misc.Annotation.EventListener;
import DiscordBotCore.Misc.Annotation.Init;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class CommandInputEvents
{
	public static final boolean DISPLAY_CHANNEL = true;
	public static final boolean DISPLAY_NAME = true;
	
	private static final int threads_amount = 4;
	private static int lastSelection = 0;
	
	private static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
	public static CopyOnWriteArrayList<CommandHandlingThread> threads = new CopyOnWriteArrayList<>();
	
	@EventListener
	public static void messageEvent( MessageReceivedEvent event )
	{
		CommandInputEvents.assignMessageToThread(event.getMessage());
	}
	
	@EventListener
	public static void messageEdit( MessageEditEvent event ) {
		if(event.getNewMessage() == null && event.getOldMessage() == null){
			System.out.println("Message edit null!");
			return;
		}
		
		if(event.getNewMessage() == null && event.getOldMessage() != null){
			CommandInputEvents.assignMessageToThread(event.getOldMessage());
		}else{
			CommandInputEvents.assignMessageToThread(event.getNewMessage());
		}
	}
	
	
	@EventListener
	private static void commandEvent(CommandExecutedEvent event){
		for(CommandHandlingThread thread : threads){
			cancelRun(thread, event.getMessage());
		}
	}
	
	@EventListener
	private static void commandFailedEvent(CommandFailedExecuteEvent event){
		for(CommandHandlingThread thread : threads){
			cancelRun(thread, event.getMessage());
		}
	}
	
	//TODO THis init isnt detected by startup init when in jar?
	@Init
	public static void init(){
		System.out.println("Starting message handling threads!");
		
		for(int i = 0; i < threads_amount; i++){
			CommandHandlingThread thread = new CommandHandlingThread(i + 1);
			thread.start();
			threads.add(thread);
		}
	}
	
	private static void startTimer(IMessage message){
		if(Thread.currentThread() instanceof CommandHandlingThread){
			CommandHandlingThread thread = (CommandHandlingThread)Thread.currentThread();
			thread.runs.put(message.getLongID(), timer.schedule(() -> errorCommand(message), 45, TimeUnit.SECONDS));
		}
	}
	
	
	private static void assignMessageToThread( IMessage message){
		ArrayList<CommandHandlingThread> thWork = new ArrayList<>();
		threads.removeIf(( th) -> !th.isAlive());
		
		if(threads.size() <= 0){
			System.err.println("No available threads for command handling! Handling message directly");
			
			for(CommandHandlingThread th : threads){
				th.stop();
			}
			
			threads.clear();
			init();
			
			handle(new MessageObject(message));
			return;
		}
		
		for(CommandHandlingThread th : CommandInputEvents.threads){
			if(!th.working && !th.isInterrupted() && th.isAlive()){
				thWork.add(th);
			}
		}
		
		if(thWork.size() > 0) {
			if (lastSelection >= thWork.size()) {
				lastSelection = 0;
			}
		}else{
			if(lastSelection >= threads.size()){
				lastSelection = 0;
			}
		}
		
		if(thWork.size() > 0){
			CommandHandlingThread th = thWork.get(lastSelection);
			th.messages.add(new MessageObject(message));
			th.getLatch().countDown();
			lastSelection++;
			
		}else{
			CommandHandlingThread th = threads.get(lastSelection);
			th.messages.add(new MessageObject(message));
			th.getLatch().countDown();
			lastSelection++;
		}
	}
	

	private static void cancelRun(CommandHandlingThread thread, IMessage message){
		if(thread.runs.containsKey(message.getLongID())) {
			thread.runs.get(message.getLongID()).cancel(true);
			thread.runs.remove(message.getLongID());
		}
	}
	
	//TODO This doesnt activate even though the command didnt excecute?
	private static void errorCommand(IMessage message){
		if(Thread.currentThread() instanceof CommandHandlingThread){
			CommandHandlingThread thread = (CommandHandlingThread)Thread.currentThread();
			thread.runs.remove(message.getLongID());
		}
		
		System.err.println("Command was not executed!");
		DiscordBotBase.reconnectBot();
	}
	
	public static void handle( MessageObject message){
		DiscordCommand command = CommandUtils.getDiscordCommand(message.getContent(), message.getChannel());
		String commandName = "";
		
		if(command != null) {
			if (command.isSubCommand()) {
				if (command != null && command.baseCommand != null)
					commandName += command.baseCommand.getClass().getSimpleName() + "/";
			}
		}
		
		if(command != null){
			if(!command.longCommandTime()) {
				startTimer(message);
			}
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
				String prefix = command instanceof DiscordCommand ? command.getCommandSign(message.getChannel()) : DiscordBotBase.getCommandSign();
				String commandN = command.isSubCommand() ? command.baseCommand.commandPrefix() + " " + command.commandPrefix() : command.commandPrefix();
				
				if(DISPLAY_CHANNEL) builder.append("[").append(message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator()).append("]");
				builder.append("Command received in private: ").append(prefix + commandN);
				
				System.out.println(builder.toString());
			}
		}
		
		if(command != null) {
			CommandUtils.executeCommand(message);
		}
	}
	
	public static class CommandHandlingThread extends Thread{
		public CommandHandlingThread( int num)
		{
			this.setName("Message-handling-thread-" + num);
		}
		public CopyOnWriteArrayList<MessageObject> messages = new CopyOnWriteArrayList<>();
		
		public MessageObject curMessage;
		public boolean working = false;
		
		public HashMap<Long, ScheduledFuture> runs = new HashMap<>();
		
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
}
