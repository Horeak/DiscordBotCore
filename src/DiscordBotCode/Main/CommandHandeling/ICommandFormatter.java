package DiscordBotCode.Main.CommandHandeling;

import sx.blah.discord.handle.obj.IMessage;

public interface ICommandFormatter
{
	IMessage getFormattedMessage(IMessage message);
	
	public static String getContentBetweenCorresponding(String s, char left, char right) {
		int pos = s.indexOf(left);
		if ( pos > -1 ) {
			int start = pos;
			int openCount = 0;
			while ( pos < s.length() ) {
				char currentChar = s.charAt(pos);
				if ( currentChar == right ) {
					if ( openCount > 1 ) // if openCount == 1 then correct one
						openCount--;
					else
						return s.substring(start + 1, pos);
				} else if ( currentChar == left )
					openCount++;
				pos++;
			}
		}
		return null;
	}
}
