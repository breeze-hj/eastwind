package eastwind.service;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.model.ElectionState;
import eastwind.model.Event;
import eastwind.rmi.RuleFactory;
import eastwind.rmi.RuleResolver;
import eastwind.support.StateFul;

public class ServiceGroup extends StateFul<ServiceGroupState> {

	private static Logger LOGGER = LoggerFactory.getLogger(ServiceGroup.class);
	
	protected String group;
	protected String version;

	protected ChannelService leader;
	protected ServiceOpener serviceOpener;
	protected ServiceMapper serviceMapper;
	protected RuleResolver ruleResolver;
	protected List<ChannelService> onlines = new CopyOnWriteArrayList<>();
	protected ServiceGroupStateListener serviceGroupStateListener;

	protected volatile int mod;
	protected Set<CompletableFuture<ChannelService>> connect1sts = new HashSet<>();

	public ServiceGroup(String group, String version, RuleFactory ruleFactory, ServiceOpener serviceOpener) {
		this.group = group;
		this.version = version;
		super.state = ServiceGroupState.INITIAL;

		ServiceStateListener serviceStateListener = new ServiceStateListener(this);
		this.serviceMapper = new ServiceMapper(new ChannelServiceFactory(group, version, serviceStateListener));
		this.ruleResolver = new RuleResolver(ruleFactory);
		this.serviceOpener = serviceOpener;

		serviceGroupStateListener = new ServiceGroupStateListener(this);
		super.setStateListener(ServiceGroupState.SERVICEABLE, (c, r) -> serviceGroupStateListener.onServiceable());
		super.setStateListener(ServiceGroupState.UNSERVICEABLE, (c, r) -> serviceGroupStateListener.onUnserviceable());
	}

	public Set<ChannelService> getAll() {
		return serviceMapper.getAll();
	}

	public List<ChannelService> getOnlines() {
		return onlines;
	}

	public void broadcast(Event event) {
		Set<ChannelService> services = serviceMapper.getAll();
		for (ChannelService service : services) {
			if (service.isShaked()) {
				service.send(event);
			}
		}
	}

	public void broadcast(List<Event> events) {
		Set<ChannelService> services = serviceMapper.getAll();
		for (ChannelService service : services) {
			service.transfer(events);
		}
	}

	public ChannelService stub(InetSocketAddress address) {
		ChannelService service = serviceMapper.stub(address);
		CompletableFuture<ChannelService> cf = service.getConnect1st();
		connect1sts.add(cf);
		cf.thenAccept(s -> connect1sts.remove(cf));
		return service;
	}

	public ChannelService map(InetSocketAddress address, String uuid) {
		return serviceMapper.map(address, uuid);
	}

	public ChannelService getService(InetSocketAddress address) {
		return serviceMapper.get(address);
	}
	
	public ChannelService getService(String uuid) {
		return serviceMapper.get(uuid);
	}

	public boolean isServiceable() {
		return getState() == ServiceGroupState.SERVICEABLE;
	}

	public boolean isUnserviceable() {
		return getState() == ServiceGroupState.UNSERVICEABLE;
	}

	public ChannelService open(InetSocketAddress address) {
		return this.serviceOpener.open(this, address);
	}

	public void open(List<InetSocketAddress> addresses) {
		this.serviceOpener.open(this, addresses);
	}

	public void online(ChannelService service) {
		onlines.add(service);
		mod++;
		ElectionState electionState = service.getElectionState();
		// is leader
		if (electionState.state == 2 && electionState.role == 1) {
			this.leader = service;
			List<InetSocketAddress> others = service.getOthers();
			for (InetSocketAddress other : others) {
				serviceOpener.open(this, other);
			}
		}
		if (electionState.leaderUuid != null && electionState.leaderAddress != null) {
			if (!electionState.leaderUuid.equals(service.getUuid())) {
				serviceOpener.open(this, electionState.leaderAddress);
			}
		}
		if (onlines.size() == 1) {
			changeState(ServiceGroupState.SERVICEABLE, null);
		}
	}

	public void suspend(ChannelService service) {
		// TODO
	}

	public void offline(ChannelService service) {
		onlines.remove(service);
		mod++;
		ServiceGroupState state = getState();
		if (state != ServiceGroupState.UNSERVICEABLE) {
			if (onlines.size() == 0 && isAllReady() && !isUnserviceable()) {
				changeState(ServiceGroupState.UNSERVICEABLE, null);
			}
		}
	}

	public boolean isAllReady() {
		return connect1sts.size() == 0;
	}

	public int getMod() {
		return mod;
	}

	public CompletableFuture<Void> waitForAll() {
		if (isAllReady()) {
			return CompletableFuture.completedFuture(null);
		}
		CompletableFuture<Void> finalCf = new CompletableFuture<Void>();
		waitForAllAdapter(finalCf);
		return finalCf;
	}

	private void waitForAllAdapter(CompletableFuture<Void> finalCf) {
		final int mod = this.mod;
		CompletableFuture<Void> cf = CompletableFuture
				.allOf(connect1sts.toArray(new CompletableFuture[connect1sts.size()]));
		cf.thenAccept(v -> {
			if (mod == this.mod) {
				finalCf.complete(null);
			} else {
				waitForAllAdapter(finalCf);
			}
		});
	}

	public RuleResolver getRuleResolver() {
		return ruleResolver;
	}

	public String getGroup() {
		return group;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return String.format("service-group[%s-%s]", group, version);
	}
}
