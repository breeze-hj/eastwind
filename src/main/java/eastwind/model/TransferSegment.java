package eastwind.model;

import java.util.Iterator;

public class TransferSegment {

	public int size;
	public int bytes;
	public long lastId;
	public Iterator<TcpObject> it;
	public Convert<TransferAck> ackConvert;

	public TransferSegment(Iterator<TcpObject> it, Convert<TransferAck> ackConvert) {
		this.it = it;
		this.ackConvert = ackConvert;
	}
	
	public TcpObject createAckObject() {
		TransferAck ack = new TransferAck();
		ack.id = lastId;
		return ackConvert.to(ack);
	}
}
