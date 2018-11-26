package eastwind.channel;

import eastwind.support.State;

/**
 * Created by jan.huang on 2018/4/11.
 */
public class ChannelState extends State {

	public static final ChannelState INITIAL = new ChannelState("INITIAL");
	
	public static final ChannelState ACTIVE = new ChannelState("ACTIVE");

	public static final ChannelState INACTIVE = new ChannelState("INACTIVE");

	public static final ChannelState SHAKED = new ChannelState("SHAKED");
	
	public static final ChannelState CLOSED = new ChannelState("CLOSED");
	
    public ChannelState(String state) {
        super(state);
    }

}
