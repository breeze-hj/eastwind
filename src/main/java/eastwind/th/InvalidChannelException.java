package eastwind.th;

public class InvalidChannelException extends EastWindException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8376307533063839053L;

	public InvalidChannelException(String group, String version, String channel) {
		super(String.format("invalid channel %s to %s-%s", channel, group, version));
	}
	
}
