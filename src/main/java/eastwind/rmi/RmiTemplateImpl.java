package eastwind.rmi;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.RmiTemplate;
import eastwind.model.RMD;
import eastwind.model.Redirect;
import eastwind.model.Request;
import eastwind.model.Response;
import eastwind.model.ThrowableInfo;
import eastwind.service.ChannelService;
import eastwind.service.ExchangeContext;
import eastwind.service.ServiceGroup;
import eastwind.support.EWUtils;
import eastwind.th.InvalidRemoteServiceException;
import eastwind.th.RmiException;

public class RmiTemplateImpl implements RmiTemplate {

	private static Logger LOGGER = LoggerFactory.getLogger(RmiTemplateImpl.class);

	private String group;
	private String version;
	private ServiceGroup serviceGroup;

	public RmiTemplateImpl(String group, String version, ServiceGroup serviceGroup) {
		this.group = group;
		this.version = version;
		this.serviceGroup = serviceGroup;
	}

	public <T> CompletableFuture<T> execute(String path, Map<Object, Object> properties, Object... args) {
		LOGGER.info("invoke to {} at {}", group, path);
		if (!serviceGroup.isAllReady()) {
			CompletableFuture<Void> brightFuture = serviceGroup.waitForAll();
			try {
				brightFuture.get(1, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOGGER.warn("", e);
			}
		}
		if (serviceGroup.isUnserviceable()) {
			return exceptionally(new InvalidRemoteServiceException(group, version));
		} else {
			List<ChannelService> onlines = serviceGroup.getOnlines();
			List<ChannelService> supply = new ArrayList<>();
			RMD rmd = RMDBuilder.from(path, args);
			List<CompletableFuture<ExchangeContext>> rmdEnquires = new ArrayList<>();
			for (ChannelService service : onlines) {
				RMDAssign rmdAssign = service.getRMDAssign();
				RMD assignTo = rmdAssign.assignFrom(rmd);
				if (assignTo == null) {
					rmdEnquires.add(service.exchange(rmd));
				} else if (assignTo.supply) {
					supply.add(service);
				}
			}

			boolean all = rmdEnquires.size() == 0;
			if (rmdEnquires.size() > 0) {
				CompletableFuture<Void> allFuture = CompletableFuture
						.allOf(rmdEnquires.toArray(new CompletableFuture[rmdEnquires.size()]));
				try {
					allFuture.get(1, TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					LOGGER.warn("", e);
				}

				for (CompletableFuture<ExchangeContext> cf : rmdEnquires) {
					if (cf.isDone()) {
						ExchangeContext ec = cf.getNow(null);
						if (ec != null) {
							ChannelService service = ec.getService();
							RMDAssign rmdAssign = service.getRMDAssign();
							RMD assignTo = rmdAssign.assignFrom(rmd);
							if (assignTo.supply) {
								supply.add(service);
							}
						}
					}
				}
				all = allFuture.isDone();
			}

			if (supply.size() == 0) {
				return exceptionally(new RmiException("Invalid RMI path or args: " + path));
			} else {
				RequestContext requestContext = new RequestContext(path, properties, args, true);
				RuleResolver ruleResolver = serviceGroup.getRuleResolver();
				Rule rule = ruleResolver.resolve(rmd, serviceGroup.getMod(), supply, all);
				ChannelService service = rule.pick(null, requestContext);
				LOGGER.info("invocation at {} by {}, choose {}", path, EWUtils.getSimpleName(rule.getClass()), service);
				RMD assignTo = service.getRMDAssign().assignFrom(rmd);

				CompletableFuture<T> finalCF = new CompletableFuture<T>();
				CompletableFuture<ExchangeContext> singleCF = sendRequest(service, assignTo, requestContext);
				singleCF.thenAccept(t -> {
					acceptResponse(finalCF, singleCF, t, rmd, requestContext);
				});
				return finalCF;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void acceptResponse(CompletableFuture<T> finalCF, CompletableFuture<ExchangeContext> singleCF,
			ExchangeContext context, RMD rmd, RequestContext requestContext) {
		if (singleCF.isDone()) {
			if (singleCF.isCompletedExceptionally()) {
				singleCF.exceptionally(th -> {
					finalCF.completeExceptionally(th);
					return null;
				});
			} else {
				Object result = context.getResult();
				if (result instanceof Response) {
					Response response = (Response) result;
					if (response.state == Response.FAILED) {
						ThrowableInfo throwableInfo = (ThrowableInfo) response.value;
						try {
							Class<?> cls = Class.forName(throwableInfo.th);
							Constructor<?> ct = cls.getConstructor(String.class);
							Throwable th = (Throwable) ct.newInstance(throwableInfo.message);
							finalCF.completeExceptionally(th);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (response.state == Response.SUCCESS) {
						finalCF.complete((T) response.value);
					}
				} else if (result instanceof Redirect) {
					requestContext.incrementRedirects();
					Redirect redirect = (Redirect) result;
					ChannelService redirectService = serviceGroup.getService(redirect.uuid);
					LOGGER.info("invocation at {} redirect to {}", requestContext.getPath(), redirectService);
					RMDAssign rmdAssign = redirectService.getRMDAssign();
					RMD assignTo = rmdAssign.assignFrom(rmd);
					final CompletableFuture<ExchangeContext> redirectCF = sendRequest(redirectService, assignTo,
							requestContext);
					redirectCF.thenAccept(t -> {
						acceptResponse(finalCF, redirectCF, t, rmd, requestContext);
					});
				}
			}
		}
	}

	private CompletableFuture<ExchangeContext> sendRequest(ChannelService service, RMD assignTo,
			RequestContext requestContext) {
		Request request = new Request();
		request.alias = assignTo.alias;
		request.properties = requestContext.getProperties();
		request.args = requestContext.getArgs();
		request.redirects = requestContext.redirects;
		return service.exchange(request);
	}

	private <T> CompletableFuture<T> exceptionally(Throwable th) {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(th);
		return future;
	}
}
