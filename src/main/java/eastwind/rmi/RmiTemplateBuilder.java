package eastwind.rmi;

import eastwind.service.BootstrapService;
import eastwind.service.BootstrapServiceable;
import eastwind.service.ServiceGroup;

public class RmiTemplateBuilder extends BootstrapServiceable {

	public RmiTemplateBuilder(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	public RmiTemplateImpl build(String group, String version) {
		ServiceGroup serviceGroup = bootstrapService.getServiceGroup(group, version);
		return new RmiTemplateImpl(group, version, serviceGroup);
	}
}
