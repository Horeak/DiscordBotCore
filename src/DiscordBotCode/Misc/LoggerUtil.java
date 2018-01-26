package DiscordBotCode.Misc;

import DiscordBotCode.DeveloperSystem.IssuesSystem.IssueBuilder;
import DiscordBotCode.DeveloperSystem.IssuesSystem.IssueHandler;
import DiscordBotCode.DeveloperSystem.IssuesSystem.IssueObject;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.CustomEvents.LogEvent;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.*;

public class LoggerUtil
{
	static final DateFormat df = new SimpleDateFormat("dd/LLL/yyyy-HH:mm(zzz)");
	static final Calendar calendar = Calendar.getInstance();
	public static Logger out;
	public static boolean ERROR_LOGS = true;
	
	public static void activate()
	{
		out = Logger.getLogger("Logger");
		out.setUseParentHandlers(false);
		out.setLevel(Level.ALL);
		
		CustomFormatter formatter = new CustomFormatter();
		CustomConsoleHandler handler = new CustomConsoleHandler();
		CustomPrintStream stream = new CustomPrintStream(System.out, out);
		CustomErrorPrintstream stream1 = new CustomErrorPrintstream(System.err, out);
		
		handler.setFormatter(formatter);
		out.addHandler(handler);
		
		stream.attachOut();
		stream1.attachOut();
	}
	
	public static void log( String text )
	{
		logInfo(text, "logs");
	}
	public static void error( String text )
	{
		logInfo(text, "errorLogs");
	}
	
	static DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	static DateFormat formatterTime = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss(zzz)");
	
	
	public static void logInfo( String text, String folder)
	{
		if(DiscordBotBase.FilePath == null) return;
		
		if(text == null || text.isEmpty() || text.equalsIgnoreCase("\n")){
			return;
		}
		
		File file = FileGetter.getFile(DiscordBotBase.FilePath + "/" + folder + "/" + "log_" + "(" + formatter.format(new Date()) + ").log");
		FileUtil.addLineToFile(file, text);
	}
	
	public static void exception( Exception e )
	{
		if (LoggerUtil.ERROR_LOGS && DiscordBotBase.FilePath != null) {
			Date today = new Date();
			
			String timePrefix = "[" + formatterTime.format(today) + "] ";
			String botVersionPrefix = "[Discord Version: " + DiscordBotBase.getVersion() + "] ";
			
			String prefix = timePrefix + botVersionPrefix;
			
			
			File file = FileGetter.getFile(DiscordBotBase.FilePath + "/errorLogs/" + formatter.format(today) + ".log");
			FileUtil.addLineToFile(file, prefix + e.getClass().getName() + ": " + e.getMessage());
			
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			
			for (StackTraceElement g : e.getStackTrace()) {
				FileUtil.addLineToFile(file, prefix + "\t at " + g);
				System.err.println("\t at " + g);
			}
			
			FileUtil.addLineToFile(file, "");
		}
	}
	
	public static void reportIssue( Exception e )
	{
		if(e instanceof InterruptedException || (e instanceof DiscordException && e.getMessage().contains("Message was unable to be sent"))){
			return;
		}
		
		IssueObject object = new IssueBuilder().genId().withException(e).withCurrentDate().build();
		
		if(!IssueHandler.issueExists(object)){
			IssueHandler.createIssue(object);
		}else{
			IssueHandler.addIssue(object);
		}
	}
}

class CustomFormatter extends Formatter
{
	public String format( LogRecord record )
	{
		StringBuilder builder = new StringBuilder(1000);
		builder.append("[").append(LoggerUtil.df.format(new Date(record.getMillis()))).append("]").append(" - ");
		
		if(record.getMessage() == null || record.getMessage().isEmpty() || record.getMessage().equalsIgnoreCase("\n")){
			return "";
		}
		
		if(record.getMessage().contains("WARN twitter4j.StatusStreamImpl")){
			return builder.toString() + " Unhandled twitter event!";
		}
		
		if (DiscordBotBase.debug) {
			builder.append("[").append(record.getLevel()).append("] - ");
			builder.append("[").append(Thread.currentThread().getName()).append(":").append(Thread.currentThread().getId()).append("] - ");
			
			
			StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
			for (StackTraceElement stack : stacks) {
				String className = stack.getClassName();
				
				if (stack.getClassName().startsWith(getClass().getPackage().getName())
				    || stack.getClassName().startsWith("sx.blah.discord")
				    || stack.isNativeMethod()
				    || className.startsWith("java")
				    || stack.getClassName().startsWith("org.slf4j")
				    || stack.getFileName().toLowerCase().contains("slf4jlogger")
				    || stack.getClassName().startsWith("org.eclipse")
				    || stack.getMethodName().equalsIgnoreCase("handleException")) {
					continue;
				}
				
				
				builder.append("[").append(stack.getFileName()).append("][").append(stack.getMethodName()).append(":").append(stack.getLineNumber()).append("] - ");
				break;
			}
		}
		
		
		builder.append(formatMessage(record));
		
		if(!record.getMessage().isEmpty()) {
			if (record.getMessage() == null || !record.getMessage().endsWith("\n")) {
				builder.append("\n");
			}
		}
		
		return builder.toString();
	}
}

class CustomConsoleHandler extends ConsoleHandler
{
	@Override
	public void publish( LogRecord record )
	{
		try {
			String message = getFormatter().format(record);
			String logMessage = message;
			if(logMessage.endsWith("\n")){
				logMessage = logMessage.substring(0, logMessage.length() - 1);
			}
			
			LoggerUtil.log(logMessage);
			
			if (record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING) {
				System.err.write(message.getBytes());
			} else {
				System.out.write(message.getBytes());
			}
			
			if(DiscordBotBase.discordClient != null && DiscordBotBase.discordClient.isReady()) {
				DiscordBotBase.discordClient.getDispatcher().dispatch(new LogEvent(message));
			}
			
		} catch (Exception exception) {
			reportError(null, exception, ErrorManager.FORMAT_FAILURE);
		}
	}
}

class CustomPrintStream extends PrintStream
{
	private Logger log;
	
	CustomPrintStream( OutputStream out, Logger log )
	{
		
		super(out, true);
		this.log = log;
	}
	
	void attachOut()
	{
		System.setOut(this);
	}
	
	@Override
	public void print( String s )
	{
		log.log(Level.INFO, s);
	}
	
	@Override
	public void println( String s )
	{
		print(s);
	}
	
	
}

class CustomErrorPrintstream extends PrintStream
{
	private Logger log;
	
	CustomErrorPrintstream( OutputStream out, Logger log )
	{
		super(out, true);
		this.log = log;
	}
	
	void attachOut()
	{
		System.setErr(this);
	}
	
	@Override
	public void print( String s )
	{
		log.log(Level.SEVERE, s);
	}
	
	@Override
	public void println( String s )
	{
		print(s);
	}
}


