package eastwind.service;

import eastwind.BaseApplication;

public class ApplicationAdapter extends BaseApplication {

	private ChannelService channelService;

	public ApplicationAdapter(ChannelService channelService) {
		this.channelService = channelService;
	}

	protected Service getService() {
		return channelService;
	}
}
