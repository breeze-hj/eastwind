package eastwind.rmi;

import java.util.List;
import java.util.Random;

import eastwind.service.ChannelService;

public class RandomRule implements Rule {

	private List<ChannelService> services;
	
	@Override
	public void prepare(List<ChannelService> services) {
		this.services = services;
	}

	@Override
	public ChannelService pick(List<ChannelService> picked, RequestContext context) {
		return services.get(new Random().nextInt(services.size()));
	}

	@Override
	public void setMeta(Object meta) {
	}
}
