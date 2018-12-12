package eastwind.service;

import java.net.InetSocketAddress;
import java.util.List;

import eastwind.channel.ChannelOpener;
import eastwind.channel.ChannelState;
import eastwind.channel.ChildChannelFactory;
import eastwind.channel.OutputChannel;

public class ServiceOpener {

	private InetSocketAddress localAddress;
	private ChannelOpener channelOpener;
	private ChildChannelFactory childChannelFactory;

	public ServiceOpener(InetSocketAddress localAddress, ChannelOpener channelOpener,
			ChildChannelFactory childChannelFactory) {
		this.localAddress = localAddress;
		this.channelOpener = channelOpener;
		this.childChannelFactory = childChannelFactory;
	}

	public void open(ServiceGroup serviceGroup, List<InetSocketAddress> addresses) {
		for (InetSocketAddress address : addresses) {
			if (!address.equals(localAddress)) {
				open(serviceGroup, address);
			}
		}
	}

	public void open(ChannelService service) {
		if (service.isOpening()) {
			for (OutputChannel channel : service.getOutputChannels()) {
				if (channel.getState() == ChannelState.CLOSED && !channel.isOpening()) {
					channelOpener.open(channel);
				}
			}
		}
		if (!service.isOpening()) {
			String group = service.getGroup();
			String version = service.getVersion();
			OutputChannel channel = childChannelFactory.newOutputChannel(group, version, service.getAddress());
			service.addChannel(channel);
			channel.bindTo(service);
			channelOpener.open(channel);
		}
	}
	
	public ChannelService open(ServiceGroup serviceGroup, InetSocketAddress address) {
		ChannelService service = serviceGroup.getService(address);
		if (service == null) {
			service = serviceGroup.stub(address);
		}
		if (service.isOpening()) {
			for (OutputChannel channel : service.getOutputChannels()) {
				if (channel.getState() != ChannelState.INACTIVE || !channel.isOpening()) {
					channelOpener.open(channel);
				}
			}
		}
		if (!service.isOpening()) {
			String group = serviceGroup.getGroup();
			String version = serviceGroup.getVersion();
			OutputChannel channel = childChannelFactory.newOutputChannel(group, version, address);
			service.addChannel(channel);
			channel.bindTo(service);
			channelOpener.open(channel);
		}
		return service;
	}

}
