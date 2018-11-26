package eastwind.channel;

import eastwind.service.BootstrapService;

public class TcpChannelStateListener<C extends TcpChannel> extends ChannelStateListener<C> {

	protected BootstrapService bootstrapService;
	
    public TcpChannelStateListener(BootstrapService bootstrapService) {
    	this.bootstrapService = bootstrapService;
	}

    public void onShaked(C channel) {
    	
    }
}
