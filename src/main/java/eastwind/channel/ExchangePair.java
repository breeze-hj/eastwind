package eastwind.channel;

import eastwind.model.TcpObject;

public class ExchangePair {

	public Long id;
	public Long respondTo;
	
	public static ExchangePair from(TcpObject tcpObject) {
		ExchangePair exchangePair = new ExchangePair();
		exchangePair.id = tcpObject.id;
		exchangePair.respondTo = tcpObject.respondTo;
		return exchangePair;
	}
	
}
