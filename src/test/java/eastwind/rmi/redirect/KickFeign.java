package eastwind.rmi.redirect;

import eastwind.annotation.Feign;

@Feign(group = "kick")
public interface KickFeign {

	Boolean kick(Object ball);
	
}
