package eastwind;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eastwind.service.Service;

public abstract class BaseApplication implements Application {

	protected HashMap<Object, Object> attributes = new HashMap<>();
	
	@Override
	public Date getStartTime() {
		return getService().getStartTime();
	}

	@Override
	public String getVersion() {
		return getService().getVersion();
	}

	@Override
	public String getUuid() {
		return getService().getUuid();
	}

	@Override
	public String getGroup() {
		return getService().getGroup();
	}

	@Override
	public Object getProperty(Object key) {
		return getService().getPropertys().get(key);
	}

	@Override
	public boolean isShutdown() {
		return getService().isShutdown();
	}

	@Override
	public InetSocketAddress getAddress() {
		return getService().getAddress();
	}

	@Override
	public Map<Object, Object> getAttributes() {
		return attributes;
	}

	protected abstract Service getService();
}
