package eastwind.eventbus;

import java.util.concurrent.ExecutionException;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;
import eastwind.EventBusConfig;

public class EventBusDemo {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		newApplicationOn(11111, "Mercury");
		newApplicationOn(12222, "Venus");
		EastWindApplication app = newApplicationOn(13333, "Earth");
		newApplicationOn(14444, "Mars");
		app.waitForOthers().get();
		app.eventBus("hello").publish("hello, brothers!");
		app.waitForShutdown().get();
	}

	public static EastWindApplication newApplicationOn(int port, String name) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("test-eventbus");
		builder.onPort(port).withProperty("name", name);
		builder.onEvents(new EventBusConfig<>("hello", (t, a, b) -> {
			System.out.println(b.getProperty("name") + "-->" + a.getProperty("name") + ": " + t);
		}));
		builder.withFixedServers(":11111,:12222,:13333,:14444");
		return builder.build();
	}
}
