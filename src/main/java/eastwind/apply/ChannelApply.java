package eastwind.apply;

import java.util.HashMap;
import java.util.Map;

import eastwind.support.EWUtils;

public class ChannelApply {

	private Map<Class<?>, Apply<?>> applys = new HashMap<>();

	public Map<Class<?>, Apply<?>> getApplys() {
		return applys;
	}

	public synchronized <T> void register(Apply<T> apply) {
		Class<?> cls = EWUtils.getInterfaceTypeArgument0(apply.getClass(), Apply.class);
		applys.put(cls, apply);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Apply<T> getApply(Class<T> cls) {
		return (Apply<T>) applys.get(cls);
	}
}
