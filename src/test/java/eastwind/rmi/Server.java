package eastwind.rmi;

import java.util.concurrent.ExecutionException;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;

public class Server {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("changjiang");
		EastWindApplication changjiang = builder.withProviders(new HelloProvider()).build();
		changjiang.waitForShutdown().get();
	}
	
}
