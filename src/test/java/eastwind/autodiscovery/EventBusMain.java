package eastwind.autodiscovery;

import java.util.concurrent.ExecutionException;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;
import eastwind.EventBusConfig;

public class EventBusMain {
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		newApplicationOn(12222, "Venus", "");
		EastWindApplication app = newApplicationOn(13333, "Earth", ":12222");
		newApplicationOn(11111, "Mercury", ":14444");
		newApplicationOn(14444, "Mars", "13333");
		app.waitForOthers().get();
		app.eventBus("hello").publish("hello, brothers!");
		app.waitForShutdown().get();
	}

	public static EastWindApplication newApplicationOn(int port, String name, String servers) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("test-eventbus");
		builder.onPort(port).withProperty("name", name);
		builder.onEvents(new EventBusConfig<>("hello", (t, a, b) -> {
			System.out.println(b.getProperty("name") + "-->" + a.getProperty("name") + ": " + t);
		}));
		builder.withFixedServers(servers);
		return builder.build();
	}
}
