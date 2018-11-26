package eastwind.service;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.EastWindApplication;
import eastwind.EventConsumer;
import eastwind.channel.ChannelOpener;
import eastwind.channel.ChildChannelFactory;
import eastwind.channel.MasterChannel;
import eastwind.channel.MasterChannelStateListener;
import eastwind.channel.OutputChannel;
import eastwind.rmi.FeignClientBuilder;
import eastwind.rmi.HashPropertyRegistry;
import eastwind.rmi.RMDRegistry;
import eastwind.rmi.RmiTemplateBuilder;
import eastwind.rmi.RuleFactory;
import eastwind.support.HashedWheelTimerExecutor;
import eastwind.support.NamedThreadFactory;

public class BootstrapService extends Service {

	private static Logger LOGGER = LoggerFactory.getLogger(BootstrapService.class);

	private EastWindApplication eastWindApplication = new EastWindApplicationAdpater(this);

	private MasterChannel masterChannel;
	private ChannelOpener channelOpener;

	private RuleFactory ruleFactory = new RuleFactory();
	private HashPropertyRegistry hashPropertyRegistry = new HashPropertyRegistry();
	private RMDRegistry rmdRegistry = new RMDRegistry(hashPropertyRegistry);
	private EventBusManager eventBusManager = new EventBusManager();
	private ServiceGroupTable serviceGroupTable = new ServiceGroupTable(new ServiceGroupFactory(ruleFactory));
	private ChildChannelFactory childChannelFactory = new ChildChannelFactory(this);

	private HashedWheelTimerExecutor hashedWheelTimerExecutor = new HashedWheelTimerExecutor();
	private ChannelRetryer channelRetryer;
	private ExecutorService customerExecutor;

	private CompletableFuture<Void> shutdownFuture = new CompletableFuture<Void>();

	public BootstrapService(String group, String version, InetSocketAddress address) {
		super.uuid = UUID.randomUUID().toString();
		super.group = group;
		super.version = version;
		super.startTime = new Date();
		super.address = address;

		serviceGroupTable.get(group, version);

		String threadPrefix = group + "@" + address.getPort();
		channelOpener = new ChannelOpener(threadPrefix);
		channelRetryer = new ChannelRetryer(channelOpener, hashedWheelTimerExecutor);

		NamedThreadFactory factory = new NamedThreadFactory(threadPrefix);
		customerExecutor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), factory);
	}

	public void start() {
		LOGGER.info("starting {}...", this);
		masterChannel = new MasterChannel(address, childChannelFactory);
		MasterChannelStateListener openListener = new MasterChannelStateListener();
		masterChannel.setChannelStateListener(openListener);
		channelOpener.open(masterChannel);
		CompletableFuture<Void> future = openListener.getOpenFuture();
		future.join();
		if (future.isCancelled() || future.isCompletedExceptionally()) {
			future.exceptionally(th -> {
				LOGGER.error("{} start fail!", this);
				LOGGER.error("", th);
				return null;
			});
			shutdown();
		} else if (future.isDone()) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				this.shutdown();
			}));
			LOGGER.info("{} start OK!", this);
		}
	}

	public ServiceGroup getServiceGroup(String group, String version) {
		return serviceGroupTable.get(group, version);
	}

	public ServiceGroup getMyServiceGroup() {
		return serviceGroupTable.get(group, version);
	}

	public void interactWith(String group, String version, List<InetSocketAddress> addresses) {
		ServiceGroup serviceGroup = serviceGroupTable.get(group, version);
		for (InetSocketAddress add : addresses) {
			if (!add.equals(address)) {
				interact(serviceGroup, add);
			}
		}
	}

	private void interact(ServiceGroup serviceGroup, InetSocketAddress address) {
		if (serviceGroup.isInteracting(address)) {
			return;
		}
		String group = serviceGroup.getGroup();
		String version = serviceGroup.getVersion();
		OutputChannel channel = childChannelFactory.newOutputChannel(group, version, address);
		ChannelService service = serviceGroup.stub(address);
		service.addChannel(channel);
		channel.bindTo(service);
		channelOpener.open(channel);
	}

	public ShakeBuilder shakeBuilder() {
		return new ShakeBuilder(this);
	}

	public RmiTemplateBuilder rmiTemplateBuilder() {
		return new RmiTemplateBuilder(this);
	}

	public FeignClientBuilder feignClientBuilder() {
		return new FeignClientBuilder(this);
	}

	public <T> DefaultEventBus<T> eventBus(String name, EventConsumer<T> eventConsumer) {
		@SuppressWarnings("unchecked")
		DefaultEventBus<T> eventBus = (DefaultEventBus<T>) eventBusManager.get(name);
		if (eventBus != null) {
			return eventBus;
		}
		eventBus = new DefaultEventBus<>(name, eventConsumer, getMyServiceGroup());
		eventBusManager.register(eventBus);
		return eventBus;
	}

	public HashPropertyRegistry getHashPropertyRegistry() {
		return hashPropertyRegistry;
	}

	public RMDRegistry getRMdRegistry() {
		return rmdRegistry;
	}

	public EventBusManager getEventBusManager() {
		return eventBusManager;
	}

	public ChannelRetryer getChannelRetryer() {
		return channelRetryer;
	}

	public RuleFactory getRuleFactory() {
		return ruleFactory;
	}

	public void shutdown() {
		channelOpener.shutdown();
		hashedWheelTimerExecutor.shutdown();
		customerExecutor.shutdown();
		shutdownFuture.complete(null);
	}

	public CompletableFuture<Void> getShutdownFuture() {
		return shutdownFuture;
	}

	public EastWindApplication getEastWindApplication() {
		return eastWindApplication;
	}

	public ExecutorService getCustomerExecutor() {
		return customerExecutor;
	}

	@Override
	public String toString() {
		return String.format("application[%s@%s (%s)]", group, address, uuid);
	}

}
