package eastwind.service;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;

import eastwind.support.StateFul;

public abstract class Service extends StateFul<ServiceState> {

	protected Date startTime;
	protected String uuid;
	protected String group;
	protected String version;
	protected Map<Object, Object> propertys;
	protected InetSocketAddress address;
	protected volatile boolean shutdown;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Map<Object, Object> getPropertys() {
		return propertys;
	}

	public void setPropertys(Map<Object, Object> propertys) {
		this.propertys = propertys;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void shutdown() {
		this.shutdown = true;
	}
    
	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

}
