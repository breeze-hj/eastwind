package eastwind.channel;

import java.net.InetSocketAddress;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class MasterChannel extends AbstractChannel implements AsyncOpenChannel {

    private InetSocketAddress address;
    private ChildChannelFactory childChannelFactory;
    
    public MasterChannel(InetSocketAddress address, ChildChannelFactory childChannelFactory) {
        this.address = address;
        this.childChannelFactory = childChannelFactory;
    }

	public InetSocketAddress getAddress() {
        return address;
    }

	public ChildChannelFactory getChildChannelFactory() {
		return childChannelFactory;
	}

}
