package DiscordBotCode.Main;

import DiscordBotCode.CommandFiles.PageSystem.ReactionEvent;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Extra.TimeUtil;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.Events.CommandInputEvents;
import DiscordBotCode.Main.CommandHandeling.Formatters.CommandDataFormat;
import DiscordBotCode.Main.CommandHandeling.Formatters.IgnoredMessageFormat;
import DiscordBotCode.Main.CommandHandeling.Formatters.MentionsMessageFormat;
import DiscordBotCode.Main.CustomEvents.BotCloseEvent;
import DiscordBotCode.Misc.LoggerUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.util.BotInviteBuilder;

import javax.management.ReflectionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordBotBase
{
	//TODO Do a proper clean up of old code and make improvements (Special command system code could need a look at)
	
	public static final String INFO_FILE_TAG = "INFO_FILE";
	private static final String VERSION_FILE_TAG = "VERSION_FILE";
	
	private static EnumSet<Permissions> permissionList = EnumSet.noneOf(Permissions.class);
	
	public static IDiscordClient discordClient;
	public static BotInviteBuilder inviteBuilder;
	
	public static DiscordBotBase discordBotBase = new DiscordBotBase();
	public static File tempFolder = null;
	
	public static boolean debug = false;
	public static boolean modules = false;
	
	public static String FilePath = "";
	public static File baseFilePath;
	
	private static String version = null;
	private static String commandSign = null;
	
	public static boolean devMode = false;
	
	private static ArrayList<String> args = null;
	
	private static ArrayList<BotLauncher> launchers = new ArrayList<>();
	
	public static void main(String[] args)
	{
		TimeUtil.startTimeTaker("upTime");
		TimeUtil.startTimeTaker("startup_time");
		
		DLLCleaner.clean();
		System.setProperty("java.awt.headless", "true");
		
		debug = System.getProperty("debugMode") != null;
		modules = System.getProperty("modules") != null;
		
		System.out.println("debug: " + debug);
		System.out.println("modules: " + modules);
		
		try {
			initFile();
			initVersion();
			initBot();

			initSubBot();
		}catch (Exception e){
			handleException(e);
		}
		
		discordBotBase.startup();
	}
	
	private static void initFile() throws IOException
	{
		String fileName = debug ? "build.debug.properties" : "build.properties";
		File infoFile = FileUtil.getFileFromStream(ClassLoader.getSystemResourceAsStream(fileName), ".properties");
		
		baseFilePath = new File(System.getProperty("user.dir") + "/");
		FileUtil.initFile(infoFile, INFO_FILE_TAG);
		FilePath = FileGetter.getFolder(baseFilePath.getPath() + FileUtil.getValue(INFO_FILE_TAG, "file_path")).getCanonicalPath();
		tempFolder = FileGetter.getFolder(DiscordBotBase.FilePath + "/tmp/");
		
		LoggerUtil.activate();
		
		infoFile.delete();
		System.out.println("File init done.");
	}
	

	private static void initVersion() throws IOException
	{
		File versionFile = FileUtil.getFileFromStream(ClassLoader.getSystemResourceAsStream(FileUtil.getValue(INFO_FILE_TAG, "version_file")), ".properties");
		
		if(versionFile != null){
			FileUtil.initFile(versionFile, VERSION_FILE_TAG);
			
			String versionFormat = FileUtil.getValue(VERSION_FILE_TAG, "version_format");
			
			String pattern = "(\\$)(.*?)(\\$)";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(versionFormat);
			
			while (m.find()) {
				String value = FileUtil.getValue(VERSION_FILE_TAG, m.group(2));
				versionFormat = versionFormat.replace(m.group(), value);
			}
			
			version = versionFormat;
		}
		
		System.out.println("Version init done.");
	}
	
	private static void initBot(){
		if(!modules) Configuration.LOAD_EXTERNAL_MODULES = false;
		
		String token = FileUtil.getValue(INFO_FILE_TAG, "token");
		
		if(token == null){
			System.err.println("Invalid bot token!");
			System.exit(0);
		}
		
		ClientBuilder builder = new ClientBuilder();
		builder.withToken(token);
		builder.withRecommendedShardCount();
		builder.withMinimumDispatchThreads(2);
		
		
		discordClient = builder.build();
		discordClient.login();
		
		commandSign = FileUtil.getValue(INFO_FILE_TAG, "command_sign");
		
		waitForReady();
		System.out.println("Bot init done.");
	}
	
	private static void initSubBot()
	{
		try {
			String launcher_class = FileUtil.getValue(INFO_FILE_TAG, "launcher_class");
			
			Reflections reflections = new Reflections(launcher_class);
			Set<Class<? extends BotLauncher>> classes = reflections.getSubTypesOf(BotLauncher.class);
			
			System.out.println("Found " + classes.size() + " bot launcher class" + (classes.size() > 1 ? "es" : "") + "!");
			
			for (Class<? extends BotLauncher> ob : classes) {
				BotLauncher launcher = ob.newInstance();
				launchers.add(launcher);
				new Thread(launcher::run).start();
			}
			
			System.out.println("Sub bot init done.");
		}catch (Exception e){
			if(e instanceof ReflectionException){
			
			}else{
				DiscordBotBase.handleException(e);
			}
		}
	}
	
	private void startup(){
		clearTmpFolder();
		
		CommandUtils.formatters.add(new IgnoredMessageFormat());
		CommandUtils.formatters.add(new MentionsMessageFormat());
		CommandUtils.formatters.add(new CommandDataFormat());
		
		ServerSettings.init();
		CommandInputEvents.init();
		registerListeners();
		
		System.out.println("Started \"" + DiscordBotBase.discordClient.getOurUser().getName() + "\" successfully!");
		System.out.println("Version \"" + getVersion() + "\"");
		System.out.println("Running on " + DiscordBotBase.discordClient.getShardCount() + " shard(s)");
		System.out.println("Found " + DiscordBotBase.discordClient.getGuilds().size() + " server(s), {" + getGuilds() + "}");
		System.out.println("Command prefix is set to \'" + getCommandSign() + "\'");
		
		Runtime.getRuntime().addShutdownHook(new Thread(DiscordBotBase::onBotClose));
		System.out.println("Startup init done.");
		
		System.out.println("Init default commands");
		BaseCommandRegister.initDefaultCommands();
		
		initCommands();
		initModules();
		
		System.out.println("System startup took: " + (System.currentTimeMillis() - TimeUtil.getStartTime("startup_time")) + "ms");
	}
	
	public static void initCommands(){
		System.out.println("Start command register");
		
		int commands = CommandUtils.discordChatCommands.size();
		
		for(BotLauncher launcher : launchers){
			launcher.registerCommands();
		}
		
		System.out.println("End command register, commands registered = " + (CommandUtils.discordChatCommands.size() - commands));
	}
	
	public static void initModules(){
		System.out.println("Start module register");
		
		int modules = CommandUtils.discordModules.size();
		
		for(BotLauncher launcher : launchers){
			launcher.registerModules();
		}
		
		System.out.println("End module register, modules registered = " + (CommandUtils.discordModules.size() - modules));
	}
	
	public static String getGuilds(){
		StringJoiner joiner = new StringJoiner(", ");
		DiscordBotBase.discordClient.getGuilds().forEach(( g ) -> joiner.add(g.getName()));
		
		return joiner.toString();
	}
	
	public static void waitForReady(){
		Long startup = 0L;
		
		while(discordClient == null || discordClient != null && !discordClient.isReady()){
			startup++;
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				DiscordBotBase.handleException(e);
			}
		}
		
		System.out.println("waitForReady: waited for " + startup + "ms");
	}
	
	protected static void registerListeners()
	{
		discordClient.getDispatcher().registerListener(new CommandInputEvents.messageListener());
		discordClient.getDispatcher().registerListener(new CommandInputEvents.messageEditedListener());
		discordClient.getDispatcher().registerListener(new ReactionEvent());
		
		discordClient.getDispatcher().registerListener(new BaseCommandRegister());
		
		discordClient.getDispatcher().registerListener((IListener<DisconnectedEvent>) ( event ) -> LoggerUtil.log("Client disconnected for reason: " + event.getReason()));
		discordClient.getDispatcher().registerListener((IListener<ReconnectFailureEvent>) ( event ) -> LoggerUtil.log("Client reconnect attempt num " + event.getCurrentAttempt() + " failed!"));
		discordClient.getDispatcher().registerListener((IListener<GuildCreateEvent>) ( event ) -> LoggerUtil.log("Bot has joined server \"" + event.getGuild().getName() + "\""));
	}
	
	public static void handleException( Exception e )
	{
		LoggerUtil.reportIssue(e);
		LoggerUtil.exception(e);
	}
	
	private static void clearTmpFolder(){
		for(File f : FileUtils.listFiles(tempFolder, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY)){
			f.delete();
		}
		
		for(File f : tempFolder.listFiles()){
			f.delete();
		}
		
		DLLCleaner.clean();
	}
	
	
	private static Long lastReconnect;
	
	public static boolean canReconnect(){
		if(lastReconnect == null) return true;
		
		Long t = System.currentTimeMillis() - lastReconnect;
		
		return t >= TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	}
	
	public static void reconnectBot(){
		boolean t = canReconnect();
		System.out.println("canReconnect() -> " + t);
		
		if(t) {
			System.out.println("Bot reconnecting!");
			discordClient.logout();
			discordClient.login();
			lastReconnect = System.currentTimeMillis();
		}
	}
	
	private static void onBotClose()
	{
		System.err.println("Shutting down bot!");
		
		discordClient.getDispatcher().dispatch(new BotCloseEvent());
		clearTmpFolder();
		
		try {
			FileUtils.deleteDirectory(tempFolder);
		} catch (IOException e) {
			DiscordBotBase.handleException(e);
		}
		
		discordClient = null;
	}
	
	public static String getVersion(){
		return version;
	}
	
	public static String getCommandSign(){
		return commandSign;
	}
	
	public static EnumSet<Permissions> getPermissionList()
	{
		return permissionList;
	}
	
	public static void setPermissionList( EnumSet<Permissions> permissionList )
	{
		DiscordBotBase.permissionList = permissionList;
		inviteBuilder = new BotInviteBuilder(discordClient).withPermissions(permissionList);
		System.out.println("Invite link: " + DiscordBotBase.inviteBuilder.build());
	}
}