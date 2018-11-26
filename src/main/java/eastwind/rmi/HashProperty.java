package eastwind.rmi;

import java.util.List;

public class HashProperty {

	private Class<?> cls;
	private List<String> propertys;

	public HashProperty(Class<?> cls, List<String> propertys) {
		this.cls = cls;
		this.propertys = propertys;
	}

	public Class<?> getCls() {
		return cls;
	}

	public List<String> getPropertys() {
		return propertys;
	}

}
