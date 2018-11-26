package eastwind.channel;

import eastwind.support.Result;
import eastwind.support.StateFul;
import io.netty.channel.Channel;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class AbstractChannel extends StateFul<ChannelState> {

	protected Channel channel;
	protected ChannelStateListener<?> channelStateListener;

	public AbstractChannel() {
		super.state = ChannelState.INITIAL;
	}

	public void setNettyChannel(Channel channel) {
		this.channel = channel;
		this.channel.closeFuture().addListener(cf -> close());
	}
	
	@SuppressWarnings("unchecked")
	public <C extends AbstractChannel, T extends ChannelStateListener<C>> void setChannelStateListener(
			T channelStateListener) {
		this.channelStateListener = channelStateListener;
		super.setStateListener(ChannelState.ACTIVE, (c, r) -> channelStateListener.onActive((C) c));
		super.setStateListener(ChannelState.INACTIVE, (c, r) -> channelStateListener.onInactive((C) c, r));
		super.setStateListener(ChannelState.CLOSED, (c, r) -> channelStateListener.onClosed((C) c));
	}

	public void active() {
		changeState(ChannelState.ACTIVE, null);
	}

	public void inactive(Throwable th) {
		changeState(ChannelState.INACTIVE, Result.fail(th));
	}
	
	public void close() {
		changeState(ChannelState.CLOSED, null);
	}
	
	public boolean isClosed() {
		return getState() == ChannelState.CLOSED;
	}
	
	public Channel getNettyChannel() {
		return channel;
	}
	
	public boolean isBright() {
		return getState() != ChannelState.INITIAL && getState() != ChannelState.ACTIVE;
	}
}
