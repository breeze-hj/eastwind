package eastwind.rmi.async;

import eastwind.EastWindApplicationBuilder;

public class Server {

	public static void main(String[] args) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("food");
		builder.withProviders(new FoodProvider()).build().waitForShutdown();
	}
	
}
