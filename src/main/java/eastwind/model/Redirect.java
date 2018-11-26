package eastwind.model;

import eastwind.Application;

public class Redirect {

	public String uuid;
	
	public static Redirect to(Application application) {
		Redirect redirect = new Redirect();
		redirect.uuid = application.getUuid();
		return redirect;
	}
	
}
