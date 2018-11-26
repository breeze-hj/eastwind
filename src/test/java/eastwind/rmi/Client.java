package eastwind.rmi;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;

public class Client {

	public static void main(String[] args) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("huanghe");
		EastWindApplication huanghe = builder.onPort(18829).withFixedServers("changjiang", ":18729").build();
		HelloFeign helloFeign = huanghe.createFeignClient(HelloFeign.class);
		
		System.out.println("changjiang return:" + helloFeign.hello("huanghe"));
		System.out.println("changjiang return:" + helloFeign.hello());
	}
	
}
