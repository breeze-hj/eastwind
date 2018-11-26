package eastwind.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

public class FeignInvocationHandler implements InvocationHandler {

	private String feignName;
	private RmiTemplateImpl rmiTemplate;

	public FeignInvocationHandler(String feignName, RmiTemplateImpl rmiTemplate) {
		this.feignName = feignName;
		this.rmiTemplate = rmiTemplate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass().equals(Object.class)) {
			return method.invoke(this, args);
		}
		String path = null;
		if (feignName == null) {
			path = method.getName();
		} else {
			path = feignName + "/" + method.getName();
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		try {
			return rmiTemplate.execute(path, null, args).get();
		} catch (ExecutionException e) {
			throw e.getCause().fillInStackTrace().fillInStackTrace();
		}
	}

}
