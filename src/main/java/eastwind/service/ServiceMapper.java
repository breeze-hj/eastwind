package eastwind.service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceMapper {

	private Map<InetSocketAddress, ChannelService> address2Services = new HashMap<>();
	private Map<String, ChannelService> uuid2Services = new HashMap<>();

	private ChannelServiceFactory channelServiceFactory;

	public ServiceMapper(ChannelServiceFactory channelServiceFactory) {
		this.channelServiceFactory = channelServiceFactory;
	}

	public ChannelService get(String uuid) {
		return uuid2Services.get(uuid);
	}

	public ChannelService get(InetSocketAddress address) {
		return address2Services.get(address);
	}

	public Set<ChannelService> getAll() {
		return new HashSet<>(address2Services.values());
	}

	public synchronized ChannelService stub(InetSocketAddress address) {
		ChannelService address2service = address2Services.get(address);
		if (address2service != null) {
			return address2service;
		}
		address2service = channelServiceFactory.create(null, address);
		address2Services.put(address, address2service);
		return address2service;
	}

	public synchronized ChannelService map(InetSocketAddress address, String uuid) {
		ChannelService uuid2service = uuid2Services.get(uuid);
		ChannelService address2service = address2Services.get(address);
		if (uuid2service == null) {
			uuid2service = mapStub(address, uuid, address2service);
		} else {
			if (address2service == null) {
				uuid2service.setAddress(address);
				address2Services.put(address, uuid2service);
			} else if (!uuid2service.getUuid().equals(address2service.getUuid())) {
				address2Services.remove(address);
				uuid2Services.remove(address2service.getUuid());
				address2Services.put(address, uuid2service);
			}
		}
		return uuid2service;
	}

	private ChannelService mapStub(InetSocketAddress address, String uuid, ChannelService address2service) {
		ChannelService uuid2service;
		if (address2service == null) {
			uuid2service = channelServiceFactory.create(uuid, address);
			uuid2Services.put(uuid, uuid2service);
			address2Services.put(address, uuid2service);
		} else {
			if (address2service.getUuid() == null || address2service.getUuid().equals(uuid)) {
				address2service.setUuid(uuid);
				uuid2service = address2service;
				uuid2Services.put(uuid, uuid2service);
			} else {
				address2Services.remove(address);
				uuid2Services.remove(address2service.getUuid());

				uuid2service = channelServiceFactory.create(uuid, address);
				uuid2Services.put(uuid, uuid2service);
				address2Services.put(address, uuid2service);
			}
		}
		return uuid2service;
	}
}
