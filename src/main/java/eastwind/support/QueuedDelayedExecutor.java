package eastwind.support;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class QueuedDelayedExecutor implements DelayedExecutor {

	private Thread t;
	private volatile DelayQueue<DelayedTask> current;
	private DelayQueue<DelayedTask>[] queues;
	private int cardinal;

	@SuppressWarnings("unchecked")
	public QueuedDelayedExecutor() {
		queues = new DelayQueue[16];
		for (int i = 0; i < queues.length; i++) {
			queues[i] = new DelayQueue<DelayedTask>();
		}
		cardinal = queues.length - 1;

		t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					DelayedTask pressing = null;
					for (DelayQueue<DelayedTask> q : queues) {
						DelayedTask dt = q.peek();
						if (dt != null && (pressing == null || dt.compareTo(pressing) < 0)) {
							pressing = dt;
							current = q;
						}
					}
					if (Thread.interrupted()) {
						continue;
					}
					try {
						if (pressing == null) {
							TimeUnit.SECONDS.sleep(2);
						} else {
							DelayedTask dt = current.poll(1, TimeUnit.SECONDS);
							if (dt != null) {
								execute(dt);
							}
						}
					} catch (InterruptedException e) {
						if (Thread.interrupted()) {
							continue;
						}
					}
				}
			}
		}, "accurately-delayed-executor");
		t.start();
	}

	public void delayExecute(DelayedTask delayedTask) {
		int i = (byte) ((delayedTask.hashCode() >> 1) & cardinal);
		DelayQueue<DelayedTask> q = queues[i];
		DelayedTask oldHead = q.peek();
		q.add(delayedTask);
		checkCurrentQueue(oldHead, q);
	}

	public void scheduleExecute(DelayedTask delayedTask, Function<Integer, Integer> periodFunc) {

	}

	public void cancel(DelayedTask delayedTask) {
		int i = (byte) ((delayedTask.hashCode() >> 1) & cardinal);
		DelayQueue<DelayedTask> q = queues[i];
		DelayedTask oldHead = q.peek();
		q.remove(delayedTask);
		checkCurrentQueue(oldHead, q);
	}

	private void checkCurrentQueue(DelayedTask oldHead, DelayQueue<DelayedTask> q) {
		if (q == current) {
			return;
		}
		DelayedTask dt = null;
		if (current == null || (dt = current.peek()) == null) {
			t.interrupt();
			return;
		}
		DelayedTask qdt = q.peek();
		if (oldHead == qdt) {
			return;
		}
		if (qdt.compareTo(dt) == -1) {
			t.interrupt();
		}
	}

}
