package eastwind.service;

import java.util.LinkedHashMap;
import java.util.Map;

import eastwind.EventBus;

public class EventBusManager {

	private Map<String, EventBus<?>> eventBuss = new LinkedHashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T> EventBus<T> get(String name) {
		return (EventBus<T>) eventBuss.get(name);
	}
	
	public void register(EventBus<?> eventBus) {
		eventBuss.put(eventBus.name(), eventBus);
	}
	
}
