package eastwind;

import java.lang.reflect.Method;
import java.util.Map;

public interface InvocationContext<T> {

	@SuppressWarnings("unchecked")
	public static <T> InvocationContext<T> getContext() {
		return (InvocationContext<T>) InvocationContextLocal.get();
	}

	EastWindApplication getMasterApplication();

	Application getRemoteApplication();

	Method getFeignMethod();

	Map<Object, Object> getInvocationPropertys();
	
	boolean isAsync();

	void async();

	boolean isCompleted();

	void complete(T result);

	T getResult();

	void completeExceptionally(Throwable th);

	Throwable getCause();

	int redirectTimes();
	
	Application getRedirectTo();

	T redirectTo(Application application);
}
