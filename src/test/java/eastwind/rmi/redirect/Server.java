package eastwind.rmi.redirect;

import java.util.concurrent.ExecutionException;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;

public class Server {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		newServerOn(18729);
		newServerOn(18829);
		newServerOn(18929).waitForShutdown().get();
	}

	private static EastWindApplication newServerOn(int port) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("kick");
		builder.onPort(port).withFixedServers(":18729,:18829,:18929");
		builder.withProviders(new KickProvider());
		return builder.build();
	}
	
}
