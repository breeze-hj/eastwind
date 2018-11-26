package eastwind.support;


import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DelayedTask implements Delayed {
	
	private Consumer<DelayedExecutor> consumer;
	private volatile boolean cancel;
	private long exeTime;

	public void setDelay(long delay) {
		this.exeTime = System.currentTimeMillis() + delay;
	}

	@Override
	public int compareTo(Delayed o) {
		if (o instanceof DelayedTask) {
			DelayedTask o1 = ( DelayedTask ) o;
			if (exeTime < o1.exeTime) {
				return -1;
			} else {
				return exeTime == o1.exeTime ? 0 : 1;
			}
		} else {
			long d1 = getDelay(TimeUnit.MILLISECONDS);
			long d2 = getDelay(TimeUnit.MILLISECONDS);
			if (d1 < d2) {
				return -1;
			} else {
				return d1 == d2 ? 0 : 1;
			}
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(exeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public void setConsumer(Consumer<DelayedExecutor> consumer) {
		this.consumer = consumer;
	}

	public Consumer<DelayedExecutor> getConsumer() {
		return consumer;
	}

	public boolean isCancel() {
		return cancel;
	}

	public void cancel() {
		this.cancel = true;
	}
}
