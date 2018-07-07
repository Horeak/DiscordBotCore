package DiscordBotCore.Main.CommandHandeling;

import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

public class ChannelObject extends Channel {
	protected MessageObject source;
	private boolean isPrivate;
	
	public ChannelObject( IChannel channel)
	{
		this((Channel)channel);
	}
	
	public ChannelObject(Channel channel)
	{
		super((DiscordClientImpl) channel.getClient(),
				channel.getName(),
				channel.getLongID(),
				channel.isPrivate() ? null : channel.getGuild(),
				channel.isPrivate() ? null : channel.getTopic(),
				channel.isPrivate() ? 0 : channel.getPosition(),
				!channel.isPrivate() && channel.isNSFW(),
				channel.getCategory() != null ? channel.getCategory().getLongID() : -1,
				channel.isPrivate() ? null : channel.roleOverrides,
				channel.isPrivate() ? null : channel.userOverrides);
		
		this.isPrivate = channel.isPrivate();
	}
	
	@Override
	public IGuild getGuild()
	{
		if(isPrivate()){
			return null;
		}
		
		return source != null && source.guild != null ? source.guild : super.guild;
	}
	
	@Override
	public boolean isPrivate() {
		return isPrivate || guild == null && (source == null || source.guild == null);
	}
}
