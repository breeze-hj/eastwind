package eastwind.service;

import eastwind.channel.TcpChannel;

public class ProcessContext {

	private Long id;
	private TcpChannel channel;
	
	public ProcessContext(Long id, TcpChannel channel) {
		this.id = id;
		this.channel = channel;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public TcpChannel getChannel() {
		return channel;
	}

	public void setChannel(TcpChannel channel) {
		this.channel = channel;
	}
	
}
