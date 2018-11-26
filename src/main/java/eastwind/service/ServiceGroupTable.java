package eastwind.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ServiceGroupTable {

	private Table<String, String, ServiceGroup> table = HashBasedTable.create();
	private ServiceGroupFactory serviceGroupFactory;
	
	public ServiceGroupTable(ServiceGroupFactory serviceGroupFactory) {
		this.serviceGroupFactory = serviceGroupFactory;
	}

	public ServiceGroup get(String group, String version) {
		ServiceGroup serviceGroup = table.get(group, version);
		if (serviceGroup == null) {
			serviceGroup = serviceGroupFactory.newServiceGroup(group, version);
			table.put(group, version, serviceGroup);
		}
		return table.get(group, version);
	}
	
	public void put(ServiceGroup serviceGroup) {
		table.put(serviceGroup.getGroup(), serviceGroup.getVersion(), serviceGroup);
	}
}
