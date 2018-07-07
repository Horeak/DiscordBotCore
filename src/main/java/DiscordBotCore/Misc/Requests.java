package DiscordBotCore.Misc;

import DiscordBotCore.Main.DiscordBotBase;
import sx.blah.discord.util.RequestBuilder;

public class Requests {
	
	public static void executeRequest( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction thenAction, RequestBuilder.IRequestAction elseAction, boolean buffer, boolean stoponException){
		RequestBuilder builder = new RequestBuilder(DiscordBotBase.discordClient);
		
		builder.doAction(action);
		if(thenAction != null) builder.andThen(thenAction);
		if(elseAction != null) builder.elseDo(elseAction);
		
		builder.shouldBufferRequests(buffer);
		builder.shouldFailOnException(stoponException);
		
		builder.build();
	}
	
	//////////////////////////////////////////////////////////////
	
	
	public static void executeRequestElse( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction elseAction, boolean buffer, boolean stopOnException){
		executeRequest(action, null, elseAction, buffer, stopOnException);
	}
	
	public static void executeRequestElse( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction elseAction, boolean buffer){
		executeRequest(action, null, elseAction, buffer, true);
	}
	
	public static void executeRequestElse( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction elseAction){
		executeRequest(action, null, elseAction, false, true);
	}
	
	//////////////////////////////////////////////////////////////
	
	
	public static void executeRequestThen( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction thenAction, boolean buffer, boolean stopOnException){
		executeRequest(action, thenAction, null, buffer, stopOnException);
	}
	
	public static void executeRequestThen( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction thenAction, boolean buffer){
		executeRequest(action, thenAction, null, buffer, true);
	}
	
	public static void executeRequestThen( RequestBuilder.IRequestAction action, RequestBuilder.IRequestAction thenAction){
		executeRequest(action, thenAction, null, false, true);
	}
	
	//////////////////////////////////////////////////////////////
	
	
	public static void executeRequest( RequestBuilder.IRequestAction action, boolean buffer, boolean stopOnException){
		executeRequest(action, null, null, buffer, stopOnException);
	}
	
	public static void executeRequest( RequestBuilder.IRequestAction action, boolean buffer){
		executeRequest(action, null, null, buffer, true);
	}
	
	public static void executeRequest( RequestBuilder.IRequestAction action){
		executeRequest(action, null, null, false, false);
	}
}
