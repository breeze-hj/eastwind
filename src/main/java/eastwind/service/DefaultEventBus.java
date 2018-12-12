package eastwind.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.EventBus;
import eastwind.EventConsumer;
import eastwind.model.Event;

public class DefaultEventBus<T> implements EventBus<T> {

	private static Logger LOGGER = LoggerFactory.getLogger(DefaultEventBus.class);

	private String name;
	private EventConsumer<T> consumer;
	private MasterServiceGroup serviceGroup;

	public DefaultEventBus(String name, EventConsumer<T> consumer, MasterServiceGroup serviceGroup) {
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

	@SuppressWarnings("unchecked")
	@Override
	public void publish(T... msgs) {
		LOGGER.info("publish {} events.", name);
		List<Event> events = Arrays.stream(msgs).map(t -> {
			Event e = new Event();
			e.name = name;
			e.data = t;
			return e;
		}).collect(Collectors.toList());
		serviceGroup.broadcast(events);
	}

	public String name() {
		return name;
	}

	public EventConsumer<T> getConsumer() {
		return consumer;
	}

}
