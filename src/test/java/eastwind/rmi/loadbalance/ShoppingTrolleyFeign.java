package eastwind.rmi.loadbalance;

import eastwind.annotation.Feign;
import eastwind.annotation.Hashable;
import eastwind.annotation.LoadBalanced;

@Feign(group = "shopping-trolley")
public interface ShoppingTrolleyFeign {

	@LoadBalanced.CONSISTENT_HASH
	ShoppingTrolley find(@Hashable int uid);
	
	@LoadBalanced.CONSISTENT_HASH
	void create(@Hashable ShoppingTrolley shoppingTrolley);
	
}
