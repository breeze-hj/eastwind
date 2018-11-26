package eastwind;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;

public interface Application {

	Date getStartTime();

	String getVersion();

	String getUuid();

	String getGroup();

	Object getProperty(Object key);

	boolean isShutdown();

	InetSocketAddress getAddress();

	Map<Object, Object> getAttributes();
}
