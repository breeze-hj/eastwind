package eastwind.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import eastwind.Application;
import eastwind.channel.OutputChannel;
import eastwind.channel.TcpChannel;
import eastwind.rmi.RMDAssign;

public class ChannelService extends Service {

	private long lastSentTime;
	private long lastRecvTime;
	private ChannelGroup channelGroup = new ChannelGroup();
	private Map<Long, ExchangeContext> exchanging = new HashMap<>();
	private Map<Long, ProcessContext> processing = new HashMap<>();
	private RMDAssign RMDAssign = new RMDAssign();
	private CompletableFuture<ChannelService> connect1st = new CompletableFuture<>();
	private Application application = new ApplicationAdapter(this);

	public ChannelService(String uuid, InetSocketAddress address, String group, String version) {
		super.uuid = uuid;
		super.address = address;
		super.group = group;
		super.version = version;
		super.state = ServiceState.INITIAL;
	}

	public void addChannel(TcpChannel tcpChannel) {
		channelGroup.addChannel(tcpChannel);
	}

	public void removeChannel(TcpChannel tcpChannel) {
		channelGroup.removeChannel(tcpChannel);
	}

	public void send(Object object) {
		if (isOnline()) {
			channelGroup.getOne().send(object);
		}
	}

	public CompletableFuture<ExchangeContext> exchange(Object message) {
		ExchangeContext exchangeContext = new ExchangeContext();
		exchangeContext.setService(this);
		exchangeContext.setSent(message);
		OutputChannel channel = channelGroup.getOne();
		exchangeContext.setChannel(channel);
		CompletableFuture<ExchangeContext> cf = new CompletableFuture<ExchangeContext>();
		Long id = channel.send(message);
		exchangeContext.setId(id);
		exchangeContext.setCf(cf);
		exchanging.put(id, exchangeContext);
		return cf;
	}

	public ExchangeContext removeExchange(Long id) {
		return exchanging.remove(id);
	}

	public void addProcess(ProcessContext processContext) {
		processing.put(processContext.getId(), processContext);
	}
	
	public ProcessContext removeProcess(Long id) {
		return processing.remove(id);
	}
	
	public boolean isConnecting() {
		return channelGroup.getOutputChannels().size() > 0;
	}

	public boolean isInitial() {
		return getState() == ServiceState.INITIAL;
	}

	public void online() {
		changeState(ServiceState.ONLINE, null);
		if (!connect1st.isDone()) {
			connect1st.complete(this);
		}
	}

	public boolean isOnline() {
		return getState() == ServiceState.ONLINE;
	}

	public void checkOffline() {
		for (OutputChannel outputChannel : channelGroup.getOutputChannels()) {
			if (!outputChannel.isClosed()) {
				return;
			}
		}
		changeState(ServiceState.OFFLINE, null);
		if (!connect1st.isDone()) {
			connect1st.complete(this);
		}
	}

	public boolean isOffline() {
		return getState() == ServiceState.OFFLINE;
	}
	
	public void setServiceStateListener(ServiceStateListener serviceStateListener) {
		super.setStateListener(ServiceState.ONLINE, (c, r) -> serviceStateListener.online((ChannelService) c));
		super.setStateListener(ServiceState.SUSPEND, (c, r) -> serviceStateListener.suspend((ChannelService) c));
		super.setStateListener(ServiceState.OFFLINE, (c, r) -> serviceStateListener.offline((ChannelService) c));
	}

	public RMDAssign getRMDAssign() {
		return RMDAssign;
	}

	public long getLastSentTime() {
		return lastSentTime;
	}

	public void resetLastSentTime() {
		this.lastSentTime = System.currentTimeMillis();
	}

	public long getLastRecvTime() {
		return lastRecvTime;
	}

	public void resetLastRecvTime() {
		this.lastRecvTime = System.currentTimeMillis();
	}

	public CompletableFuture<ChannelService> getConnect1st() {
		return connect1st;
	}

	public Application getApplication() {
		return application;
	}

	@Override
	public String toString() {
		return String.format("application[%s@%s(%s)]", group, address, uuid);
	}
}
