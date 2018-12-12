package eastwind.service;

import eastwind.model.ElectionState;
import eastwind.model.Shake;

public class ShakeBuilder extends BootstrapServiceable {

	public ShakeBuilder(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	public Shake build(boolean properties) {
		Shake shake = new Shake();
		shake.uuid = bootstrapService.uuid;
		shake.group = bootstrapService.group;
		shake.version = bootstrapService.version;
		shake.address = bootstrapService.address;
		shake.startTime = bootstrapService.startTime;
		if (properties) {
			shake.properties = bootstrapService.propertys;
		}
		MasterServiceGroup serviceGroup = bootstrapService.getMasterServiceGroup();
		ElectionState electionState = serviceGroup.getElectionState();
		shake.electionState = electionState;
		return shake;
	}
}
