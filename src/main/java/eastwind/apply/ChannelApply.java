package eastwind.apply;

import java.util.HashMap;
import java.util.Map;

public class ChannelApply {

	private Map<Class<?>, Apply<?>> applys = new HashMap<>();
	
	public <T> void register(Class<T> cls, Apply<T> apply) {
		applys.put(cls, apply);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Apply<T> getApply(Class<T> cls) {
		return (Apply<T>) applys.get(cls);
	}
}
