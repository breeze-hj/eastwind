package eastwind.channel;

import java.net.InetSocketAddress;

import eastwind.apply.Apply;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class OutputChannel extends TcpChannel implements AsyncOpenChannel {

	private int openMod;
	private int retrys;
	private boolean opening;
	private InetSocketAddress remoteAddress;

	public OutputChannel(String group, String version, InetSocketAddress remoteAddress) {
		this.group = group;
		this.version = version;
		this.remoteAddress = remoteAddress;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setChannelStateListener(ChannelStateListener channelStateListener) {
		OutputChannelStateListener listener = (OutputChannelStateListener) channelStateListener;
		super.setChannelStateListener(listener);
		super.setStateListener(ChannelState.SHAKED, (c, r) -> listener.onShaked((OutputChannel) c));
	}

	public void incrementRetrys() {
		retrys++;
	}

	public void resetRetrys() {
		if (this.retrys != 0) {
			this.retrys = 0;
		}
	}

	public int getOpenMod() {
		return openMod;
	}
	
	public void incrementOpenMod() {
		this.openMod++;
	}
	
	public int getRetrys() {
		return retrys;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public String getGroup() {
		return group;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return "OutputChannel [group=" + group + ", version=" + version + ", channel=" + channel + "]";
	}

	public boolean isOpening() {
		return opening;
	}

	public void setOpening(boolean opening) {
		this.opening = opening;
	}

	@Override
	protected Object applyExt(Apply<Object> apply, Object ext, ExchangePair exchangePair) {
		return apply.applyFromOutputChannel(this, ext, exchangePair);
	}

}
