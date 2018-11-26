package eastwind.rmi;

import eastwind.InvocationContext;

public class HelloProvider implements HelloFeign {

	@Override
	public String hello(String group) {
		return "hello, " + group + "!";
	}

	@Override
	public String hello() {
		InvocationContext<String> context = InvocationContext.getContext();
		String group = context.getRemoteApplication().getGroup();
		return "hello, " + group + "!";
	}

}
