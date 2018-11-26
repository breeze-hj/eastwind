package eastwind.service;

import java.net.InetSocketAddress;

public class ChannelServiceFactory {

	private String group;
	private String version;
	private ServiceStateListener serviceStateListener;
	
	public ChannelServiceFactory(String group, String version, ServiceStateListener serviceStateListener) {
		this.group = group;
		this.version = version;
		this.serviceStateListener = serviceStateListener;
	}
	
	public ChannelService create(String uuid, InetSocketAddress address) {
		ChannelService service = new ChannelService(uuid, address, group, version);
		service.setServiceStateListener(serviceStateListener);
		return service;
	}
	
}
