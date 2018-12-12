package eastwind.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import eastwind.Application;
import eastwind.channel.ChannelState;
import eastwind.channel.OutputChannel;
import eastwind.channel.TcpChannel;
import eastwind.channel.TransferBuffer;
import eastwind.model.ElectionState;
import eastwind.rmi.RMDAssign;

public class ChannelService extends Service {

	private long lastSentTime;
	private long lastRecvTime;
	private boolean shaked;
	private ChannelGroup channelGroup = new ChannelGroup();
	private Map<Long, ExchangeContext> exchanging = new HashMap<>();
	private Map<Long, ProcessContext> processing = new HashMap<>();
	private TransferBuffer transferBuffer = new TransferBuffer("#");
	private RMDAssign RMDAssign = new RMDAssign();
	private ElectionState electionState;
	private List<InetSocketAddress> others;
	private Application application = new ApplicationAdapter(this);
	private CompletableFuture<ChannelService> connect1st = new CompletableFuture<>();

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

	public synchronized CompletableFuture<ExchangeContext> exchange(Object message) {
		ExchangeContext exchangeContext = new ExchangeContext();
		exchangeContext.setService(this);
		exchangeContext.setSent(message);
		OutputChannel channel = channelGroup.getOne();
		exchangeContext.setChannel(channel);
		CompletableFuture<ExchangeContext> cf = new CompletableFuture<ExchangeContext>();
		exchangeContext.setCf(cf);
		if (channel.getState() == ChannelState.SHAKED) {
			Long id = channel.send(message);
			exchangeContext.setId(id);
			exchanging.put(id, exchangeContext);
		} else {
			ForkJoinPool.commonPool().execute(() -> {
				waitForReady(channel);
				Long id = channel.send(message);
				exchangeContext.setId(id);
				exchanging.put(id, exchangeContext);
			});
		}
		return cf;
	}

	// TODO improve
	private void waitForReady(OutputChannel channel) {
		for (int i = 0; i < 100; i++) {
			if (channel.getState() == ChannelState.SHAKED) {
				break;
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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

	public void transfer(Object object) {
		transferBuffer.add(object);
		tryTransfer(transferBuffer);
	}

	public void transfer(List<Object> objects) {
		for (Object obj : objects) {
			transferBuffer.add(obj);
		}
		tryTransfer(transferBuffer);
	}

	private void tryTransfer(TransferBuffer transferBuffer) {
		if (!transferBuffer.isTransferring()) {
			transferBuffer.setTransferring(true);
			channelGroup.getOne().transfer(transferBuffer);
		}
	}

	public boolean isOpening() {
		return channelGroup.getOutputChannels().size() > 0;
	}

	public boolean isInitial() {
		return getState() == ServiceState.INITIAL;
	}

	public boolean isShaked() {
		return shaked;
	}

	public void online() {
		if (!shaked) {
			shaked = true;
		}
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

	public TransferBuffer getTransferBuffer() {
		return transferBuffer;
	}

	public ElectionState getElectionState() {
		return electionState;
	}

	public void setElectionState(ElectionState electionState) {
		this.electionState = electionState;
	}

	public List<OutputChannel> getOutputChannels() {
		return channelGroup.getOutputChannels();
	}

	public List<InetSocketAddress> getOthers() {
		return others;
	}

	public void setOthers(List<InetSocketAddress> others) {
		this.others = others;
	}

	@Override
	public String toString() {
		return String.format("remote[%s@%s]", group, address);
	}
}
