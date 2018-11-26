package eastwind.rmi;

import eastwind.annotation.Feign;

@Feign(group = "changjiang")
public interface HelloFeign {
	
	String hello();
	
	String hello(String group);
}
