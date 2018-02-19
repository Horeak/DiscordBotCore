package DiscordBotCode.Main.CommandHandeling;

import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

public class ChannelObject extends Channel {
	protected MessageObject source;
	
	public ChannelObject( IChannel channel)
	{
		this((Channel)channel);
	}
	
	public ChannelObject(Channel channel)
	{
		super((DiscordClientImpl) channel.getClient(),
				channel.getName(),
				channel.getLongID(),
				channel.getGuild(),
				channel.getTopic(),
				channel.getPosition(),
				channel.isNSFW(),
				channel.getCategory() != null ? channel.getCategory().getLongID() : -1,
				channel.roleOverrides,
				channel.userOverrides);
	}
	
	@Override
	public IGuild getGuild()
	{
		return source != null && source.guild != null ? source.guild : super.guild;
	}
}
