package DiscordBotCode.Main;

import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.DeveloperSystem.DevAccess;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Extra.TimeUtil;
import DiscordBotCode.Main.CommandHandeling.CommandUtils;
import DiscordBotCode.Main.CommandHandeling.Events.CommandInputEvents;
import DiscordBotCode.Main.CommandHandeling.Formatters.CommandDataFormat;
import DiscordBotCode.Main.CommandHandeling.Formatters.IgnoredMessageFormat;
import DiscordBotCode.Main.CommandHandeling.Formatters.MentionsMessageFormat;
import DiscordBotCode.Main.CustomEvents.BotCloseEvent;
import DiscordBotCode.Main.CustomEvents.CommandRegisterEvent;
import DiscordBotCode.Main.CustomEvents.CommandRemoveEvent;
import DiscordBotCode.Main.CustomEvents.InitEvents.InitCommandRegisterEvent;
import DiscordBotCode.Main.CustomEvents.InitEvents.InitModuleRegisterEvent;
import DiscordBotCode.Misc.LoggerUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.util.BotInviteBuilder;

import javax.management.ReflectionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
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
	public static boolean extraFeedback = false;
	public static boolean modules = false;
	
	public static String FilePath = "";
	public static File baseFilePath;
	
	private static String version = null;
	private static String commandSign = null;
	
	public static boolean devMode = false;
	
	private static ArrayList<String> args = null;
	
	public static ConcurrentHashMap<Long, ArrayList<String>> ignoredRoles = new ConcurrentHashMap<>();
	
	public static void main(String[] args)
	{
		DLLCleaner.clean();
		TimeUtil.startTimeTaker("upTime");
		
		debug = System.getProperty("debug") != null;
		extraFeedback = System.getProperty("info") != null;
		modules = System.getProperty("modules") != null;
		
		System.out.println("debug: " + debug);
		System.out.println("extraFeedback: " + extraFeedback);
		System.out.println("modules: " + modules);
		
		try {
			initFile();
			initVersion();
			initBot();

			initSubBot();
			initRoleIgnores();
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
		
		discordClient = builder.build();
		discordClient.login();
		
		commandSign = FileUtil.getValue(INFO_FILE_TAG, "command_sign");
		
		waitForReady();
		System.out.println("Bot init done.");
	}
	
	private static void initRoleIgnores() throws IOException {
		File infoFile = FileUtil.getFileFromStream(ClassLoader.getSystemResourceAsStream("ignored_roles.properties"), ".properties");
		
		final int[] roles = { 0 };
		
		Files.lines(infoFile.toPath()).forEach(( e ) -> {
			String[] t = e.split("\\|\\$");
			
			if(Utils.isLong(t[0])){
				if(!ignoredRoles.containsKey(Long.parseLong(t[0]))){
					ignoredRoles.put(Long.parseLong(t[0]), new ArrayList<>());
				}
				
				ignoredRoles.get(Long.parseLong(t[0])).add(t[1]);
				roles[ 0 ] += 1;
			}
		});
		
		System.out.println("Added " + roles[0] + " role ignore rules across " + ignoredRoles.size() + " guilds.");
		System.out.println("Role ignore init done.");
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
	
	private static void initSubBot()
	{
		try {
			String launcher_class = FileUtil.getValue(INFO_FILE_TAG, "launcher_class");
			
			Reflections reflections = new Reflections(launcher_class);
			Set<Class<? extends BotLauncher>> classes = reflections.getSubTypesOf(BotLauncher.class);
			
			System.out.println("Found " + classes.size() + " bot launcher class" + (classes.size() > 1 ? "es" : "") + "!");
			
			for (Class<? extends BotLauncher> ob : classes) {
				BotLauncher launcher = ob.newInstance();
				launcher.run();
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
		CommandInputEvents.init();
		clearTmpFolder();
		
		registerListeners();
		
		new Thread(() -> {
			waitForReady();
			
			TimeUtil.startTimeTaker("start_time");
			
			discordClient.getDispatcher().registerListener(new CommandInputEvents.messageListener());
			discordClient.getDispatcher().registerListener(new CommandInputEvents.messageEditedListener());
			
			StringJoiner joiner = new StringJoiner(", ");
			DiscordBotBase.discordClient.getGuilds().forEach(( g ) -> joiner.add(g.getName()));
			
			System.out.println("Started \"" + DiscordBotBase.discordClient.getOurUser().getName() + "\" successfully!");
			System.out.println("Version \"" + getVersion() + "\"");
			System.out.println("Running on " + DiscordBotBase.discordClient.getShardCount() + " shard(s)");
			System.out.println("Found " + DiscordBotBase.discordClient.getGuilds().size() + " server(s), {" + joiner.toString() + "}");
			System.out.println("Command prefix is set to \'" + getCommandSign() + "\'");
			
			ServerSettings.init();
		}).start();
		
		//Commands thread
		new Thread(() -> {
			waitForReady();
			
			CommandUtils.formatters.add(new IgnoredMessageFormat());
			CommandUtils.formatters.add(new MentionsMessageFormat());
			CommandUtils.formatters.add(new CommandDataFormat());
			
			discordClient.getDispatcher().dispatch(new InitModuleRegisterEvent());
			discordClient.getDispatcher().dispatch(new InitCommandRegisterEvent());
		}).start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(DiscordBotBase::onBotClose));
		
		System.out.println("Startup init done.");
	}
	
	public static void waitForReady(){
		while(discordClient == null || discordClient != null && !discordClient.isReady()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				handleException(e);
			}
		}
	}
	
	protected static void registerListeners()
	{
		discordClient.getDispatcher().registerListener(new BaseCommandRegister());
		
		discordClient.getDispatcher().registerListener((IListener<DisconnectedEvent>) ( event ) -> LoggerUtil.log("Client disconnected for reason: " + event.getReason()));
		discordClient.getDispatcher().registerListener((IListener<ReconnectSuccessEvent>) ( event ) -> {
			TimeUtil.startTimeTaker("upTime"); //Reset uptime when reconnected
			LoggerUtil.log("Client reconnected successfully");
		});
		
		discordClient.getDispatcher().registerListener((IListener<ReconnectFailureEvent>) ( event ) -> LoggerUtil.log("Client reconnect attempt num " + event.getCurrentAttempt() + " failed!"));
		discordClient.getDispatcher().registerListener((IListener<GuildCreateEvent>) ( event ) -> LoggerUtil.log("Bot has joined server \"" + event.getGuild().getName() + "\""));
	}
	
	public static void handleException( Exception e )
	{
		LoggerUtil.reportIssue(e);
		LoggerUtil.exception(e);
	}
	
	public static void handleExceptionSilently( Exception e )
	{
		LoggerUtil.exception(e);
		DevAccess.msgDevs("```perl\nA exception has occurred!\n\n > Exception: " + ExceptionUtils.getStackTrace(e).replace("\n", "\n\t\t\t") + "\n```");
	}
	
	public static void handleExceptionSilently( String e )
	{
		LoggerUtil.exception(new Exception(e));
		DevAccess.msgDevs("```perl\nA exception has occurred!\n\n > Info: " + e + "\n```");
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
	
	public static void addModule( DiscordModule module, String key )
	{
		CommandUtils.discordModules.put(key, module);
		DiscordBotBase.discordClient.getModuleLoader().loadModule(module);
	}
	
	public static void stopModule( String key )
	{
		CommandUtils.discordModules.get(key).disable();
	}
	
	public static void resumeModule( String key )
	{
		CommandUtils.discordModules.get(key).enable(DiscordBotBase.discordClient);
	}
	
	public static void removeModule( String key )
	{
		DiscordBotBase.discordClient.getModuleLoader().unloadModule(CommandUtils.discordModules.get(key));
		CommandUtils.discordModules.remove(key);
	}
	
	
	public static void registerCommand( Class<? extends DiscordChatCommand> classObj, String key )
	{
		try{
			DiscordChatCommand command = classObj.newInstance();
			
			if(command == null){
				return;
			}
			
			if (!CommandUtils.discordChatCommands.containsKey(key)) {
				command.initPermissions();
				CommandUtils.discordChatCommands.put(key, command);
				DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandRegisterEvent(command, key));
			} else {
				System.err.println("Unable to register command with key: " + key + ", command already exists");
			}
			
		}catch (Exception e){
			DiscordBotBase.handleException(e);
		}
	}
	
	public static void unRegisterCommand( String key )
	{
		if (CommandUtils.discordChatCommands.containsKey(key)) {
			DiscordBotBase.discordClient.getDispatcher().dispatch(new CommandRemoveEvent(CommandUtils.discordChatCommands.get(key), key));
			CommandUtils.discordChatCommands.remove(key);
		} else {
			System.err.println("Unable to remove no existing command with key: " + key);
		}
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
		
		new Thread(() -> {
			DiscordBotBase.waitForReady();
			
			inviteBuilder = new BotInviteBuilder(discordClient).withPermissions(permissionList);
			System.out.println("Invite link: " + DiscordBotBase.inviteBuilder.build());
		}).start();
	}
}