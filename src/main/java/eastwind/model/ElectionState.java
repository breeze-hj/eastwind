package eastwind.model;

import java.net.InetSocketAddress;

public class ElectionState {

	public int term;
	public int role;
	public int state;
	public String leaderUuid;
	public InetSocketAddress leaderAddress;
	
}
