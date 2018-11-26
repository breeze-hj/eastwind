package eastwind.service;

import eastwind.rmi.RuleFactory;

public class ServiceGroupFactory {

	private RuleFactory ruleFactory;

	public ServiceGroupFactory(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}
	
	public ServiceGroup newServiceGroup(String group, String version) {
		return new ServiceGroup(group, version, ruleFactory);
	}
}
