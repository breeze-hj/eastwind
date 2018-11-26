package eastwind.channel;

import eastwind.apply.Apply;
import io.netty.channel.Channel;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class InputChannel extends TcpChannel {

	public InputChannel(Channel channel) {
		setNettyChannel(channel);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setChannelStateListener(ChannelStateListener channelStateListener) {
		InputChannelStateListener listener = (InputChannelStateListener) channelStateListener;
		super.setChannelStateListener(listener);
		super.setStateListener(ChannelState.SHAKED, (c, r) -> listener.onShaked((InputChannel) c));
	}

	@Override
	public String toString() {
		return "InputChannel [group=" + group + ", version=" + version + ", channel=" + channel + "]";
	}
	
	public void setGroup(String group) {
		this.group = group;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	protected Object applyExt(Apply<Object> apply, Object ext, TransferContext transferContext) {
		return apply.applyFromInputChannel(this, ext, transferContext);
	}
}
