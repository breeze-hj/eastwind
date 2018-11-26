package eastwind.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.EventBus;
import eastwind.EventConsumer;
import eastwind.model.Event;

public class DefaultEventBus<T> implements EventBus<T> {

	private static Logger LOGGER = LoggerFactory.getLogger(DefaultEventBus.class);
	
	private String name;
	private EventConsumer<T> consumer;
	private ServiceGroup serviceGroup;

	public DefaultEventBus(String name, EventConsumer<T> consumer, ServiceGroup serviceGroup) {
		this.name = name;
		this.consumer = consumer;
		this.serviceGroup = serviceGroup;
	}

	public void publish(T msg) {
		LOGGER.info("publish {} event.", name);
		Event event = new Event();
		event.name = name;
		event.data = msg;
		serviceGroup.broadcast(event);
	}

	public String name() {
		return name;
	}

	public EventConsumer<T> getConsumer() {
		return consumer;
	}

}
