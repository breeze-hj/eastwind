package eastwind.channel;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class TransferBuffer implements Iterator<TransferItem> {

	private long syncId;
	private String name;
	private long ackId;
	private boolean transferring;
	private AtomicLong id = new AtomicLong();

	private TransferItem first;
	private TransferItem transfered;
	private TransferItem last;

	public TransferBuffer(String name) {
		this.name = name;
	}

	public void add(Object obj) {
		TransferItem item = new TransferItem(id.incrementAndGet(), obj);
		if (first == null) {
			first = item;
			last = item;
		} else {
			last.next = item;
		}
	}

	public boolean hasNext() {
		return transfered != last;
	}

	public TransferItem next() {
		if (first == null) {
			return null;
		}
		if (transfered == null) {
			transfered = first;
			return first;
		} else {
			if (transfered.next == null) {
				return null;
			} else {
				transfered = transfered.next;
				return transfered;
			}
		}
	}

	public void ack(Long id) {
		for (; first != null && first.id < id; first = first.next) {
		}
		this.ackId = id;
		first.msg = null;
	}

	public long getSyncId() {
		return syncId;
	}

	public void reset(Long id) {
		ack(id);
		transfered = first;
	}

	public long getAckId() {
		return ackId;
	}

	public boolean isTransferring() {
		return transferring;
	}

	public void setTransferring(boolean transferring) {
		this.transferring = transferring;
	}

	public String getName() {
		return name;
	}
}
