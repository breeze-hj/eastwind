package eastwind.rmi;

import java.util.HashMap;
import java.util.Map;

public class HashPropertyRegistry {

	private Map<Class<?>, HashProperty> hashPropertys = new HashMap<>();
	
	public void register(HashProperty hashProperty) {
		hashPropertys.put(hashProperty.getCls(), hashProperty);
	}
	
	public HashProperty get(Class<?> cls) {
		return hashPropertys.get(cls);
	}
}
