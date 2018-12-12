package eastwind.model;

import java.util.Map;

public class TcpObject {

	public Long id;
	public Long respondTo;
	public Long transferId;
	public Integer type;
	public boolean hasHeader;
	public int args;
	public Object ext;

	public transient int bytes;
	public transient Map<Object, Object> header;
	public transient Object body;

	@Override
	public String toString() {
		return "TcpObject [id=" + id + ", respondTo=" + respondTo + ", type=" + type + ", hasHeader=" + hasHeader
				+ ", ext=" + ext + "]";
	}

}
