package eastwind.apply;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import eastwind.Application;
import eastwind.EastWindApplication;
import eastwind.InvocationContext;
import eastwind.support.EWUtils;

public class DefaultInvocationContext<T> implements InvocationContext<T> {

	static ThreadLocal<InvocationContext<?>> THREAD_LOCAL = new ThreadLocal<>();
	
	private EastWindApplication eastWindApplication;
	private Application remoteApplication;
	private Method method;

	private Long id;
	private Map<Object, Object> propertys;
	private boolean async;
	private boolean done;
	private T result;
	private Throwable cause;
	private Application redirectTo;
	private int redirectTimes;
	private CompletableFuture<Void> cf = new CompletableFuture<Void>();

	public DefaultInvocationContext(Long id, EastWindApplication eastWindApplication, Application remoteApplication,
			Method method, Map<Object, Object> propertys, int redirectTimes) {
		this.id = id;
		this.eastWindApplication = eastWindApplication;
		this.remoteApplication = remoteApplication;
		this.method = method;
		this.propertys = propertys;
		this.redirectTimes = redirectTimes;
	}

	public Long getId() {
		return id;
	}

	@Override
	public EastWindApplication getMasterApplication() {
		return eastWindApplication;
	}

	@Override
	public Application getRemoteApplication() {
		return remoteApplication;
	}

	@Override
	public Method getFeignMethod() {
		return method;
	}

	@Override
	public Map<Object, Object> getInvocationPropertys() {
		return propertys;
	}
	
	@Override
	public boolean isAsync() {
		return async;
	}

	@Override
	public void async() {
		this.async = true;
		this.cf = new CompletableFuture<Void>();
	}

	@Override
	public boolean isCompleted() {
		return done;
	}

	@Override
	public void complete(T result) {
		this.done = true;
		this.result = result;
		this.cf.complete(null);
	}

	@Override
	public T getResult() {
		return result;
	}

	@Override
	public void completeExceptionally(Throwable cause) {
		this.cause = cause;
		this.cf.complete(null);
	}

	@Override
	public Throwable getCause() {
		return cause;
	}
	
	@Override
	public Application getRedirectTo() {
		return redirectTo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T redirectTo(Application redirectTo) {
		this.redirectTo = redirectTo;
		this.cf.complete(null);
		return (T) EWUtils.returnNull(method.getReturnType());
	}

	public CompletableFuture<Void> getCf() {
		return cf;
	}

	@Override
	public int redirectTimes() {
		return redirectTimes;
	}
	
	public void incrementRedirectTimes() {
		this.redirectTimes++;
	}

}
