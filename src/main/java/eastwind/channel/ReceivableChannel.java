package eastwind.channel;

public abstract class ReceivableChannel<T> extends AbstractChannel {

	protected long lastSentTime;
	protected long lastRecvTime;
	
	public abstract void recv(T msg);
	
	public long getLastSentTime() {
		return lastSentTime;
	}

	public void resetLastSentTime() {
		this.lastSentTime = System.currentTimeMillis();
	}

	public long getLastRecvTime() {
		return lastRecvTime;
	}

	public void resetLastRecvTime() {
		this.lastRecvTime = System.currentTimeMillis();
	}
}
