package eastwind.rmi;

import java.util.Map;

public class RequestContext {

	private String path;
	public int redirects;
	private Map<Object, Object> properties;
	private Object[] args;
	private boolean proxy;

	public RequestContext(String path, Map<Object, Object> properties, Object[] args, boolean proxy) {
		this.path = path;
		this.properties = properties;
		this.args = args;
		this.proxy = proxy;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<Object, Object> properties) {
		this.properties = properties;
	}

	public int getRedirects() {
		return redirects;
	}
	
	public void incrementRedirects() {
		this.redirects++;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public boolean isProxy() {
		return proxy;
	}

	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}
}
