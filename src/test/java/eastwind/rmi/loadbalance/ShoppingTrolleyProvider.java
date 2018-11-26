package eastwind.rmi.loadbalance;

import java.util.HashMap;
import java.util.Map;

public class ShoppingTrolleyProvider implements ShoppingTrolleyFeign {

	private Map<Integer, ShoppingTrolley> shoppingTrolleys = new HashMap<>();
	
	@Override
	public ShoppingTrolley find(int uid) {
		return shoppingTrolleys.get(uid);
	}

	@Override
	public void create(ShoppingTrolley shoppingTrolley) {
		shoppingTrolleys.put(shoppingTrolley.getUid(), shoppingTrolley);
	}

}
