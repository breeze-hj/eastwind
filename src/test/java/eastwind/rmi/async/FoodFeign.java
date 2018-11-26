package eastwind.rmi.async;

import eastwind.annotation.Feign;

@Feign(group = "food")
public interface FoodFeign {

	String cook(String food);
	
}
