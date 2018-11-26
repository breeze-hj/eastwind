package eastwind.model;

import java.util.Map;

import eastwind.apply.Asynchronously;

public class Request implements Asynchronously {

	public String alias;
	public int redirects;
	public Map<Object, Object> properties;
	public Object[] args;
	
}
