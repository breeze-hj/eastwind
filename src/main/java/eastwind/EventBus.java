package eastwind;

public interface EventBus<T> {

	String name();
	
	void publish(T msg);
	
}
