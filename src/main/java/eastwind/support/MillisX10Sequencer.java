package eastwind.support;

public class MillisX10Sequencer implements Sequencer {

	private long last;

	public MillisX10Sequencer() {
		this.last = System.currentTimeMillis() * 10;
	}

	public synchronized long get() {
		long now = System.currentTimeMillis() * 10;
		if (now <= last) {
			now = ++last;
		} else {
			last = now;
		}
		return now;
	}
}