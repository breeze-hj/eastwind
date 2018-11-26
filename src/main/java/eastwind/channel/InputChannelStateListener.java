package eastwind.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.service.BootstrapService;
import eastwind.service.ChannelService;
import eastwind.support.Result;

/**
 * Created by jan.huang on 2018/4/18.
 */
public class InputChannelStateListener extends TcpChannelStateListener<InputChannel> {

	private static Logger LOGGER = LoggerFactory.getLogger(InputChannelStateListener.class);
	
	public InputChannelStateListener(BootstrapService bootstrapService) {
		super(bootstrapService);
	}
	
	@Override
	public void onActive(InputChannel channel) {
		LOGGER.debug("channel active: {}", channel.getNettyChannel());
	}

	@Override
	public void onInactive(InputChannel channel, Result<?> result) {
		LOGGER.debug("channel inactive: {}", channel.getNettyChannel());
		ChannelService service = channel.getService();
		if (service != null) {
			service.removeChannel(channel);
		}
	}
	
	@Override
    public void onShaked(InputChannel channel) {
		LOGGER.debug("channel shaked: {}", channel);
    }
}
