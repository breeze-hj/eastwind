package eastwind;

import eastwind.service.ElectionPolicy;
import eastwind.service.Service;

public class LocalPolicy implements ElectionPolicy {

	@Override
	public int compare(Service o1, Service o2) {
		return Integer.compare(o1.getAddress().getPort(), o2.getAddress().getPort());
	}

}
