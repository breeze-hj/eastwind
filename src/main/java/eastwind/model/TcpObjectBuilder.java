package eastwind.model;

import java.util.Map;

import eastwind.support.MillisX10Sequencer;
import eastwind.support.Sequencer;

public class TcpObjectBuilder {

	private static Sequencer SEQUENCER = new MillisX10Sequencer();
	private TcpObject tcpObject = new TcpObject();

	public static TcpObjectBuilder newBuilder(Integer type) {
		TcpObjectBuilder builder = new TcpObjectBuilder();
		builder.tcpObject.id = SEQUENCER.get();
		builder.tcpObject.type = type;
		return builder;
	}

	public static TcpObjectBuilder newExtBuilder(Object ext) {
		TcpObjectBuilder builder = new TcpObjectBuilder();
		builder.tcpObject.id = SEQUENCER.get();
		builder.tcpObject.type = TcpObjectType.BY_EXT;
		builder.tcpObject.ext = ext;
		return builder;
	}

	public TcpObject build() {
		return tcpObject;
	}

	public TcpObjectBuilder respondTo(TcpObject from) {
		tcpObject.respondTo = from.id;
		return this;
	}

	public TcpObjectBuilder ext(Object ext) {
		tcpObject.ext = ext;
		return this;
	}

	public TcpObjectBuilder header(Map<Object, Object> header) {
		tcpObject.header = header;
		tcpObject.hasHeader = true;
		return this;
	}

	public TcpObjectBuilder body(Object body) {
		if (body != null) {
			tcpObject.args = 1;
			tcpObject.body = body;
		}
		return this;
	}

	public TcpObjectBuilder bodys(Object[] body) {
		if (body != null) {
			tcpObject.args = body.length;
			tcpObject.body = body;
		}
		return this;
	}
}
