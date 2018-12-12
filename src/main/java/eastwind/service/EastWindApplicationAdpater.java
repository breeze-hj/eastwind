package eastwind.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import eastwind.Application;
import eastwind.BaseApplication;
import eastwind.EastWindApplication;
import eastwind.EventBus;
import eastwind.RmiTemplate;

public class EastWindApplicationAdpater extends BaseApplication implements EastWindApplication {

	private BootstrapService bootstrapService;

	public EastWindApplicationAdpater(BootstrapService bootstrapService) {
		this.bootstrapService = bootstrapService;
	}

	@Override
	public <T> T createFeignClient(Class<T> feign) {
		return bootstrapService.feignClientBuilder().build(feign);
	}

	@Override
	public RmiTemplate createRmiTemplate(String group, String version) {
		return bootstrapService.rmiTemplateBuilder().build(group, version);
	}

	@Override
	public <T> EventBus<T> eventBus(String name) {
		return bootstrapService.getEventBusManager().get(name);
	}

	@Override
	public CompletableFuture<Void> waitForOthers() {
		return bootstrapService.getMasterServiceGroup().waitForElectAndAll();
	}

	@Override
	public List<Application> getOthers(boolean online) {
		Set<ChannelService> services = bootstrapService.getMasterServiceGroup().getAll();
		return services.stream().filter(s -> !online || s.isOnline()).map(s -> s.getApplication()).collect(Collectors.toList());
	}

	protected Service getService() {
		return bootstrapService;
	}

	@Override
	public CompletableFuture<Void> waitForShutdown() {
		if (bootstrapService.isShutdown()) {
			return CompletableFuture.completedFuture(null);
		}
		return bootstrapService.getShutdownFuture().thenAccept(v -> {});
	}
}
