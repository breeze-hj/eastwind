package eastwind.rmi.async;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;
import eastwind.RmiTemplate;

public class Client {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("dinner");
		builder.onPort(8729).withFixedServers("food", "18729");
		EastWindApplication application = builder.build();
		RmiTemplate rmiTemplate = application.createRmiTemplate("food");
		Map<Object, Object> propertys = new HashMap<Object, Object>();
		propertys.put("eggs", 2);
		CompletableFuture<String> cf = rmiTemplate.execute("/cook", propertys, "egg-fried-rice");
		cf.thenAccept(s -> {
			System.out.println("your " + s + " is done!");
		});
		application.waitForShutdown().get();
	}
	
}
