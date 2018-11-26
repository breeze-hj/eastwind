package eastwind.rmi;

import java.util.List;

import eastwind.service.ChannelService;

public interface Rule {

	void setMeta(Object meta);
	
	void prepare(List<ChannelService> services);

	ChannelService pick(List<ChannelService> picked, RequestContext context);

}
