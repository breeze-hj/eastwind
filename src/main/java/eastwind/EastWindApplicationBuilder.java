package eastwind;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import eastwind.service.BootstrapService;

public class EastWindApplicationBuilder {

	private String group;
	private String version;
	private InetSocketAddress address;
	private Map<Object, Object> propertys = new HashMap<>();
	private Set<EventBusConfig<?>> eventBusConfigs = new HashSet<>();
	private Set<HashPropertyBuilder<?>> builders = new HashSet<>();
	private Set<Object> providers = new HashSet<>();
	private Table<String, String, List<InetSocketAddress>> table = HashBasedTable.create();

	public static EastWindApplicationBuilder newBuilder(String group) {
		return newBuilder(group, "default");
	}

	public static EastWindApplicationBuilder newBuilder(String group, String version) {
		EastWindApplicationBuilder builder = new EastWindApplicationBuilder();
		builder.group = group;
		builder.version = version;
		return builder;
	}

	private EastWindApplicationBuilder() {

	}

	public EastWindApplicationBuilder onPort(int port) {
		this.address = new InetSocketAddress(port);
		return this;
	}

	public EastWindApplicationBuilder onAddress(InetSocketAddress address) {
		this.address = address;
		return this;
	}

	public EastWindApplicationBuilder onEvents(EventBusConfig<?>... eventBusConfigs) {
		Arrays.stream(eventBusConfigs).forEach(t -> this.eventBusConfigs.add(t));
		return this;
	}

	public EastWindApplicationBuilder withHashPropertyBuilders(HashPropertyBuilder<?>... builders) {
		Arrays.stream(builders).forEach(t -> this.builders.add(t));
		return this;
	}

	public EastWindApplicationBuilder withProviders(Object... providers) {
		Arrays.stream(providers).forEach(t -> this.providers.add(t));
		return this;
	}

	public EastWindApplicationBuilder withEtcd(String etcd) {
		return this;
	}

	public EastWindApplicationBuilder withProperty(Object key, Object value) {
		propertys.put(key, value);
		return this;
	}
	
	public EastWindApplicationBuilder withPropertys(Map<Object, Object> propertys) {
		propertys.putAll(propertys);
		return this;
	}
	
	public EastWindApplicationBuilder withFixedServers(String address) {
		return withFixedServers(group, version, address);
	}

	public EastWindApplicationBuilder withFixedServers(String group, String address) {
		return withFixedServers(group, "default", address);
	}
	
	public EastWindApplicationBuilder withFixedServers(String group, String version, String address) {
		List<InetSocketAddress> current = table.get(group, version);
		if (current == null) {
			current = new ArrayList<>();
			table.put(group, version, current);
		}
		List<String> l = Splitter.onPattern("[,;]").trimResults().omitEmptyStrings().splitToList(address);
		for (String t : l) {
			List<String> l2 = Splitter.on(":").trimResults().omitEmptyStrings().splitToList(t);
			if (l2.size() == 1) {
				InetSocketAddress add = new InetSocketAddress(Integer.parseInt(l2.get(0)));
				current.add(add);
			} else if (l2.size() > 1) {
				InetSocketAddress add = new InetSocketAddress(l2.get(0), Integer.parseInt(l2.get(1)));
				current.add(add);
			}
		}
		return this;
	}

	public EastWindApplication build() {
		if (address == null) {
			address = new InetSocketAddress(18729);
		}
		BootstrapService bootstrapService = new BootstrapService(group, version, address);
		bootstrapService.setPropertys(propertys);
		eventBusConfigs.forEach(t -> bootstrapService.eventBus(t.getName(), t.getEventConsumer()));
		builders.forEach(t -> bootstrapService.getHashPropertyRegistry().register(t.build()));
		providers.forEach(t -> bootstrapService.getRMdRegistry().register(t));
		table.cellSet().forEach(t -> bootstrapService.interactWith(t.getRowKey(), t.getColumnKey(), t.getValue()));

		bootstrapService.start();
		return bootstrapService.getEastWindApplication();
	}
}
