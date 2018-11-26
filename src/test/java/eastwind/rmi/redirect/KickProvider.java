package eastwind.rmi.redirect;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import eastwind.Application;
import eastwind.InvocationContext;

public class KickProvider implements KickFeign {

	@Override
	public Boolean kick(Object ball) {
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
		}
		if (new Random().nextInt(20) == 0) {
			return true;
		}
		InvocationContext<Boolean> context = InvocationContext.getContext();
		int times = context.redirectTimes();
		if (times >= 10) {
			throw new RuntimeException("Reach TTL!");
		}
		List<Application> others = context.getMasterApplication().getOthers(true);
		Application redirectTo = others.get(new Random().nextInt(others.size()));
		return context.redirectTo(redirectTo);
	}

}
