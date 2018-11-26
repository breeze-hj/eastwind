package eastwind.channel;

import eastwind.model.TcpObject;

public class TransferContext {

	public Long id;
	public Long respondTo;
	
	public static TransferContext from(TcpObject tcpObject) {
		TransferContext transferContext = new TransferContext();
		transferContext.id = tcpObject.id;
		transferContext.respondTo = tcpObject.respondTo;
		return transferContext;
	}
	
}
