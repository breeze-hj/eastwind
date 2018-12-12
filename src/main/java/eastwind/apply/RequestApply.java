package eastwind.apply;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.Application;
import eastwind.EastWindApplication;
import eastwind.InvocationContextLocal;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.ExchangePair;
import eastwind.model.Convert;
import eastwind.model.Redirect;
import eastwind.model.Request;
import eastwind.model.Response;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;
import eastwind.model.ThrowableInfo;
import eastwind.rmi.RMDRegistry;
import eastwind.rmi.RmiEntity;

public class RequestApply implements Apply<Request> {

	private static Logger LOGGER = LoggerFactory.getLogger(RequestApply.class);
	
	private EastWindApplication eastWindApplication;
	private RMDRegistry rmdRegister;
	private ExecutorService customerExecutor;

	public RequestApply(EastWindApplication eastWindApplication, RMDRegistry rmdRegister,
			ExecutorService customerExecutor) {
		this.eastWindApplication = eastWindApplication;
		this.rmdRegister = rmdRegister;
		this.customerExecutor = customerExecutor;
	}

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Request request, ExchangePair exchangePair) {
		LOGGER.info("apply request at {} from {}.", request.alias, inputChannel.getService());
		RmiEntity rmiEntity = rmdRegister.getEntity(request.alias);
		Method method = rmiEntity.getMethod();
		Object target = rmiEntity.getTarget();
		Application remote = inputChannel.getService().getApplication();
		DefaultInvocationContext<?> context = new DefaultInvocationContext<>(exchangePair.id, eastWindApplication,
				remote, method, request.properties, request.redirects);

		customerExecutor.execute(() -> {
			InvocationContextLocal.set(context);
			Object result = null;
			Throwable th = null;
			try {
				if (request.args == null || request.args.length == 0) {
					result = method.invoke(target);
				} else {
					result = method.invoke(target, request.args);
				}
			} catch (InvocationTargetException e) {
				th = e.getTargetException();
			} catch (Throwable e) {
				th = e;
			}
			if (th != null) {
				respondExceptionally(inputChannel, exchangePair, th);
			} else {
				if (context.isAsync()) {
					context.getCf().thenAccept(t -> {
						if (context.isCompleted()) {
							respondNormally(inputChannel, exchangePair, context.getResult());
						} else if (context.getCause() != null) {
							respondExceptionally(inputChannel, exchangePair, context.getCause());
						} else if (context.getRedirectTo() != null) {
							respondRedirected(inputChannel, exchangePair, context.getRedirectTo());
						}
					});
				} else {
					if (context.getRedirectTo() != null) {
						respondRedirected(inputChannel, exchangePair, context.getRedirectTo());
					} else {
						respondNormally(inputChannel, exchangePair, result);
					}
				}
			}
			InvocationContextLocal.set(null);
		});
		return null;
	}

	private void respondNormally(InputChannel inputChannel, ExchangePair exchangePair, Object result) {
		Response response = new Response();
		response.state = Response.SUCCESS;
		response.value = result;
		inputChannel.respond(exchangePair.id, response);
	}

	private void respondExceptionally(InputChannel inputChannel, ExchangePair exchangePair, Throwable th) {
		Response response = new Response();
		response.state = Response.FAILED;
		ThrowableInfo info = new ThrowableInfo();
		info.th = th.getClass().getName();
		info.message = th.getMessage();
		response.value = info;
		inputChannel.respond(exchangePair.id, response);
	}

	private void respondRedirected(InputChannel inputChannel, ExchangePair exchangePair,
			Application application) {
		inputChannel.respond(exchangePair.id, Redirect.to(application));
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Request t, ExchangePair exchangePair) {
		return null;
	}

	@Override
	public Convert<Request> getConvert() {
		return new Convert<Request>() {
			@Override
			public void build(Request t, TcpObjectBuilder builder) {
				builder.header(t.properties).bodys(t.args);
			}

			@Override
			public void init(TcpObject tcpObject, Request t) {
				t.properties = tcpObject.header;
				t.args = (Object[]) tcpObject.body;
			}
		};
	}

}
