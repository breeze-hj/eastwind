package eastwind.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceStateListener {

	private static Logger LOGGER = LoggerFactory.getLogger(ServiceStateListener.class);
	
	private ServiceGroup serviceGroup;

	public ServiceStateListener(ServiceGroup serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public void online(ChannelService service) {
		LOGGER.info("{} is online.", service);
		serviceGroup.online(service);
	}

	public void suspend(ChannelService service) {
		LOGGER.info("{} is supended.", service);
		serviceGroup.suspend(service);
	}

	public void offline(ChannelService service) {
		LOGGER.info("{} is offline.", service);
		serviceGroup.offline(service);
	}

}
