package eastwind.rmi.loadbalance;

import java.util.List;

public class ShoppingTrolley {

	private int uid;
	private List<String> products;
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public List<String> getProducts() {
		return products;
	}
	public void setProducts(List<String> products) {
		this.products = products;
	}
}
