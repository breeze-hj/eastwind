package eastwind;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EastWindApplication extends Application {

	 <T> T createFeignClient(Class<T> feign);
	 
	 default RmiTemplate createRmiTemplate(String group) {
		 return createRmiTemplate(group, "default");
	 }
	 
	 RmiTemplate createRmiTemplate(String group, String version);
	 
	 <T> EventBus<T> eventBus(String name);
	 
	 CompletableFuture<Void> waitForOthers();
	 
	 List<Application> getOthers(boolean online);
	 
	 CompletableFuture<Void> waitForShutdown();
}
