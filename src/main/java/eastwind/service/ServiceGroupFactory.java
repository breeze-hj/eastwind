package eastwind.service;

import eastwind.rmi.RuleFactory;

public class ServiceGroupFactory {

	private RuleFactory ruleFactory;
	private ServiceOpener serviceOpener;

	public ServiceGroupFactory(RuleFactory ruleFactory, ServiceOpener serviceOpener) {
		this.ruleFactory = ruleFactory;
		this.serviceOpener = serviceOpener;
	}
	
	public ServiceGroup newServiceGroup(String group, String version) {
		return new ServiceGroup(group, version, ruleFactory, serviceOpener);
	}
}
