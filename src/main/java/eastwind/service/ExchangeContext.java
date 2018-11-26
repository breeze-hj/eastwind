package eastwind.service;

import java.util.concurrent.CompletableFuture;

import eastwind.channel.OutputChannel;

public class ExchangeContext {

	private Long id;
	private Object sent;
	private Object result;
	private OutputChannel channel;
	private ChannelService service;
	private CompletableFuture<ExchangeContext> cf;

	public void complete(Object result) {
		this.result = result;
		cf.complete(this);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Object getSent() {
		return sent;
	}

	public void setSent(Object sent) {
		this.sent = sent;
	}

	public OutputChannel getChannel() {
		return channel;
	}

	public void setChannel(OutputChannel channel) {
		this.channel = channel;
	}

	public ChannelService getService() {
		return service;
	}

	public void setService(ChannelService service) {
		this.service = service;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public CompletableFuture<ExchangeContext> getCf() {
		return cf;
	}

	public void setCf(CompletableFuture<ExchangeContext> cf) {
		this.cf = cf;
	}
}
