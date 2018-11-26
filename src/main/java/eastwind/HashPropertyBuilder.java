package eastwind;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eastwind.rmi.HashProperty;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class HashPropertyBuilder<T> {

	private Class<T> cls;
	private T target;
	private List<String> propertys = new ArrayList<>();

	public HashPropertyBuilder(Class<T> cls) {
		this.cls = cls;
		
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setSuperclass(cls);
		@SuppressWarnings("unchecked")
		Class<T> proxyClass = (Class<T>) proxyFactory.createClass();
		T targetWrap = null;
		try {
			targetWrap = (T) proxyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProxyObject proxyObject = (ProxyObject) targetWrap;
		proxyObject.setHandler(new MethodHandler() {
			@Override
			public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				if (!thisMethod.getDeclaringClass().equals(Object.class)) {
					String property = getProperty(thisMethod);
					if (property != null) {
						if (!propertys.contains(property)) {
							propertys.add(property);
						}
					}
				}
				return proceed.invoke(self, args);
			}
		});
		this.target = targetWrap;
	}

	private String getProperty(Method method) {
		if (method.getParameterTypes().length == 0 && method.getReturnType() != Void.class) {
			String name = method.getName();
			if (name.startsWith("is")) {
				name = name.replaceFirst("is", "");
			} else if (name.startsWith("get")) {
				name = name.replaceFirst("get", "");
			} else {
				return null;
			}
			char c = name.charAt(0);
			if (name.length() > 0 && c >= 'A' && c <= 'Z') {
				if (name.length() == 1) {
					return String.valueOf(c).toLowerCase();
				} else {
					return String.valueOf(c).toLowerCase() + name.substring(1);
				}
			}
		}
		return null;
	}

	
	public Class<T> getCls() {
		return cls;
	}

	public T getTarget() {
		return target;
	}

	public List<String> getPropertys() {
		return propertys;
	}
	
	public HashProperty build() {
		return new HashProperty(cls, propertys);
	}
}
