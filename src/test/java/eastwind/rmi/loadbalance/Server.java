package eastwind.rmi.loadbalance;

import java.util.concurrent.ExecutionException;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;
import eastwind.HashPropertyBuilder;

public class Server {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		newApplicationOn(18729);
		newApplicationOn(18829);
		newApplicationOn(18929).waitForShutdown().get();
	}

	public static EastWindApplication newApplicationOn(int port) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("shopping-trolley");
		builder.onPort(port).withFixedServers(":18729,:18829,:18929");
		builder.withProviders(new ShoppingTrolleyProvider());

		HashPropertyBuilder<ShoppingTrolley> hashPropertyBuilder = new HashPropertyBuilder<>(ShoppingTrolley.class);
		hashPropertyBuilder.getTarget().getUid();
		builder.withHashPropertyBuilders(hashPropertyBuilder);
		
		return builder.build();
	}
}
