package eastwind.channel;

public class TransferItem {

	public long id;
	public Object msg;
	public TransferItem next;

	public TransferItem(long id, Object msg) {
		this.id = id;
		this.msg = msg;
	}

}
