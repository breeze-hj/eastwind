package eastwind.rmi;

import java.lang.reflect.Method;

public class RmiEntity {

	private String alias;
	private Method method;
	private Object target;

	public RmiEntity(String alias, Method method, Object target) {
		this.alias = alias;
		this.method = method;
		this.target = target;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Method getMethod() {
		return method;
	}

	public Object getTarget() {
		return target;
	}

}
