package eastwind.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceGroupStateListener {

	private static Logger LOGGER = LoggerFactory.getLogger(ServiceGroupStateListener.class);

	private ServiceGroup serviceGroup;
	
	public ServiceGroupStateListener(ServiceGroup serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public void onServiceable() {
		LOGGER.info("{} is serviceable.", serviceGroup);
	}
	
	public void onUnserviceable() {
		LOGGER.info("{} is unserviceable.", serviceGroup);
	}
}
