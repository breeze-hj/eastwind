package eastwind;

public class EventBusConfig<T> {

	private String name;
	private EventConsumer<T> eventConsumer;
	
	public EventBusConfig(String name, EventConsumer<T> eventConsumer) {
		this.name = name;
		this.eventConsumer = eventConsumer;
	}
	
	public String getName() {
		return name;
	}
	
	public EventConsumer<T> getEventConsumer() {
		return eventConsumer;
	}
}
