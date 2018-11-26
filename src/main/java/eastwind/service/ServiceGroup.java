package eastwind.service;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import eastwind.model.Event;
import eastwind.rmi.RuleFactory;
import eastwind.rmi.RuleResolver;
import eastwind.support.StateFul;

public class ServiceGroup extends StateFul<ServiceGroupState> {

	private String group;
	private String version;

	private ServiceMapper serviceMapper;
	private RuleResolver ruleResolver;
	private List<ChannelService> onlines = new CopyOnWriteArrayList<>();
	private ServiceGroupStateListener serviceGroupStateListener;

	private int mod;
	private Set<CompletableFuture<ChannelService>> connect1sts = new HashSet<>();

	public ServiceGroup(String group, String version, RuleFactory ruleFactory) {
		this.group = group;
		this.version = version;
		super.state = ServiceGroupState.INITIAL;

		ServiceStateListener serviceStateListener = new ServiceStateListener(this);
		this.serviceMapper = new ServiceMapper(new ChannelServiceFactory(group, version, serviceStateListener));
		this.ruleResolver = new RuleResolver(ruleFactory);

		serviceGroupStateListener = new ServiceGroupStateListener(this);
		super.setStateListener(ServiceGroupState.SERVICEABLE, (c, r) -> serviceGroupStateListener.onServiceable());
		super.setStateListener(ServiceGroupState.UNSERVICEABLE, (c, r) -> serviceGroupStateListener.onUnserviceable());
	}

	public List<ChannelService> getAll() {
		return serviceMapper.getAll();
	}
	
	public List<ChannelService> getOnlines() {
		return onlines;
	}

	public void broadcast(Event event) {
		for (ChannelService online : onlines) {
			online.send(event);
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

	public ChannelService getService(String uuid) {
		return serviceMapper.get(uuid);
	}
	
	public boolean isServiceable() {
		return getState() == ServiceGroupState.SERVICEABLE;
	}

	public boolean isUnserviceable() {
		return getState() == ServiceGroupState.UNSERVICEABLE;
	}

	public void online(ChannelService service) {
		onlines.add(service);
		mod++;
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
			if (onlines.size() == 0 && isAllBright() && !isUnserviceable()) {
				changeState(ServiceGroupState.UNSERVICEABLE, null);
			}
		}
	}

	public boolean isAllBright() {
		return connect1sts.size() == 0;
	}

	public int getMod() {
		return mod;
	}

	public CompletableFuture<Void> waitForBright() {
		if (isAllBright()) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.allOf(connect1sts.toArray(new CompletableFuture[connect1sts.size()]));
	}

	public boolean isInteracting(InetSocketAddress address) {
		ChannelService service = serviceMapper.get(address);
		if (service == null) {
			return false;
		}
		return service.isConnecting();
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
