package eastwind;

public interface EventConsumer<T> {

	void accept(T t, EastWindApplication master, Application remote);
	
}
