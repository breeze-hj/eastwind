package eastwind.channel;

import eastwind.apply.Apply;
import eastwind.apply.Asynchronously;
import eastwind.apply.ChannelApply;
import eastwind.model.Convert;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;
import eastwind.model.TcpObjectType;
import eastwind.service.ChannelService;
import eastwind.service.ProcessContext;
import io.netty.channel.Channel;

/**
 * Created by jan.huang on 2018/4/17.
 */
public abstract class TcpChannel extends ReceivableChannel<TcpObject> {

	protected String group;
	protected String version;
	protected ChannelService service;

	protected ChannelApply channelApply;

	@SuppressWarnings("unchecked")
	public Long send(Object message) {
		Apply<Object> apply = (Apply<Object>) channelApply.getApply(message.getClass());
		Convert<Object> convert = apply.getConvert();
		TcpObject tcpObject = convert.to(message);
		send0(tcpObject);
		return tcpObject.id;
	}

	private void send0(TcpObject tcpObject) {
		Channel nettyChannel = getNettyChannel();
		nettyChannel.writeAndFlush(tcpObject);
		resetLastSentTime();
		if (service != null) {
			service.resetLastSentTime();
		}
	}

	@Override
	public void recv(TcpObject tcpObject) {
		resetLastRecvTime();
		TcpObject back = recv0(tcpObject);
		if (back != null) {
			back.respondTo = tcpObject.id;
			send0(back);
		}
	}

	@SuppressWarnings("unchecked")
	private TcpObject recv0(TcpObject tcpObject) {
		if (service != null) {
			service.resetLastRecvTime();
		}
		switch (tcpObject.type) {
		case TcpObjectType.PING:
			TcpObject pong = TcpObjectBuilder.newBuilder(TcpObjectType.PONG).build();
			service.resetLastSentTime();
			return pong;
		case TcpObjectType.PONG:
			break;
		case TcpObjectType.SHUTDOWN:
			if (service != null && !service.isShutdown()) {
				service.shutdown();
			}
			break;
		case TcpObjectType.BY_EXT:
			Apply<Object> apply = (Apply<Object>) channelApply.getApply(tcpObject.ext.getClass());
			Object ext = apply.getConvert().from(tcpObject);
			if (ext instanceof Asynchronously) {
				ProcessContext processContext = new ProcessContext(tcpObject.id, this);
				this.service.addProcess(processContext);
			}
			Object result = applyExt(apply, ext, TransferContext.from(tcpObject));
			if (result == null) {
				return null;
			} else {
				if (!result.getClass().equals(ext.getClass())) {
					apply = (Apply<Object>) channelApply.getApply(result.getClass());
				}
				return apply.getConvert().to(result);
			}
		default:
			break;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void respond(Long id, Object response) {
		this.service.removeProcess(id);
		Apply<Object> apply = (Apply<Object>) channelApply.getApply(response.getClass());
		TcpObject tcpObject = apply.getConvert().to(response);
		tcpObject.respondTo = id;
		send0(tcpObject);
	}
	
	protected abstract Object applyExt(Apply<Object> apply, Object ext, TransferContext transferContext);

	public void bindTo(ChannelService service) {
		this.service = service;
	}

	public void shake() {
		changeState(ChannelState.SHAKED, null);
	}

	public ChannelService getService() {
		return service;
	}

	public void setChannelApply(ChannelApply channelApply) {
		this.channelApply = channelApply;
	}

	public ChannelApply getChannelApply() {
		return channelApply;
	}

}
