package eastwind.channel;

import eastwind.support.Result;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class ChannelStateListener<C extends AbstractChannel> {

    public void onActive(C channel) {
    	
    }

    public void onInactive(C channel, Result<?> result) {
    	
    }
    
    public void onClosed(C channel) {
    	
    }
}
