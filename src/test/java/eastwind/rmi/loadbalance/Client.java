package eastwind.rmi.loadbalance;

import com.google.common.collect.Lists;

import eastwind.EastWindApplication;
import eastwind.EastWindApplicationBuilder;

public class Client {

	public static void main(String[] args) {
		EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("test-hash");
		builder.onPort(7777).withFixedServers("shopping-trolley", ":18729,:18829,:18929");
		EastWindApplication application = builder.build();
		ShoppingTrolleyFeign shoppingTrolleyFeign = application.createFeignClient(ShoppingTrolleyFeign.class);

		ShoppingTrolley shoppingTrolley = new ShoppingTrolley();
		shoppingTrolley.setUid(999);
		shoppingTrolley.setProducts(Lists.newArrayList("pc", "pad", "phone"));
		
		shoppingTrolleyFeign.create(shoppingTrolley);
		ShoppingTrolley queryd = shoppingTrolleyFeign.find(999);
		System.out.println(queryd.getProducts());
	}

}
