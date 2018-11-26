package eastwind.rmi.redirect;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;

public class Client {

	public static void main(String[] args) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("server");
		builder.onPort(7777).withFixedServers("kick", ":18729,:18829,:18929");
		EastWindApplication client = builder.build();
		KickFeign kickFeign = client.createFeignClient(KickFeign.class);
		Object ball = new Object();
		if (kickFeign.kick(ball)) {
			System.out.println("serve succeeded!");
		}
	}

}
