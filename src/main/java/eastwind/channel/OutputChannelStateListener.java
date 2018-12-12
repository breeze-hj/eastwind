package eastwind.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.model.Shake;
import eastwind.service.BootstrapService;
import eastwind.service.ChannelService;
import eastwind.support.Result;

/**
 * Created by jan.huang on 2018/4/19.
 */
public class OutputChannelStateListener extends TcpChannelStateListener<OutputChannel> {

	private static Logger LOGGER = LoggerFactory.getLogger(OutputChannelStateListener.class);

	public OutputChannelStateListener(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	@Override
	public void onActive(OutputChannel channel) {
		LOGGER.debug("channel active: {}", channel);
		channel.resetRetrys();
		channel.setOpening(false);
		Shake shake = bootstrapService.shakeBuilder().build(true);
		channel.send(shake);
	}

	@Override
	public void onInactive(OutputChannel channel, Result<?> result) {
		LOGGER.debug("channel inactive: {}", channel);
	}

	@Override
	public void onClosed(OutputChannel channel) {
		ChannelService service = channel.getService();
		if (!service.isOffline()) {
			service.checkOffline();
		}
		channel.setOpening(false);
		if (!service.isShutdown()) {
			bootstrapService.getChannelRetryer().retry(channel);
		}
	}

	@Override
	public void onShaked(OutputChannel channel) {
		LOGGER.debug("channel shaked: {}", channel);
		ChannelService service = channel.getService();
		if (!service.isOnline() && !service.isShutdown()) {
			service.online();
		}
	}

}
