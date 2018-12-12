package eastwind.model;

import java.net.InetSocketAddress;

public class Vote {

	// 10选举 11选举广播 12转移 99接受
	// 20其他candidate 22转移candidate
	// 30产生leader
	public int type;
	public String leaderUuid;
	public InetSocketAddress leaderAddress;
	public Object data;
	
	@Override
	public String toString() {
		return "Vote [type=" + type + ", leaderAddress=" + leaderAddress + "]";
	}
	
	
}
