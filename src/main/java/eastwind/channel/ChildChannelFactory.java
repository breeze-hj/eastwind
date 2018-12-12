package eastwind.channel;

import java.net.InetSocketAddress;

import com.fasterxml.jackson.databind.ObjectMapper;

import eastwind.apply.ChannelApply;
import eastwind.apply.EventApply;
import eastwind.apply.ShakeApply;
import eastwind.http.ActuatorController;
import eastwind.http.HttpRequestDispatcher;
import eastwind.rmi.HashPropertyRegistry;
import eastwind.rmi.RMDRegistry;
import eastwind.service.BootstrapService;
import eastwind.service.BootstrapServiceable;
import io.netty.channel.Channel;

public class ChildChannelFactory extends BootstrapServiceable {

	private ShakeApply shakeApply;
	private EventApply eventApply;

	private HttpRequestDispatcher actuatorDispatcher;
	private HttpRequestDispatcher providerDispatcher;

	public ChildChannelFactory(BootstrapService bootstrapService) {
		super(bootstrapService);
		shakeApply = new ShakeApply(bootstrapService);
		eventApply = new EventApply(bootstrapService.getEastWindApplication(), bootstrapService.getEventBusManager());

		ObjectMapper objectMapper = new ObjectMapper();
		RMDRegistry rmdRegistry = new RMDRegistry(new HashPropertyRegistry());
		rmdRegistry.register(new ActuatorController(bootstrapService));
		actuatorDispatcher = new HttpRequestDispatcher(rmdRegistry, objectMapper);

		providerDispatcher = new HttpRequestDispatcher(bootstrapService.getRMdRegistry(), objectMapper);
	}

	public InputChannel newInputChannel(Channel nettyChannel) {
		InputChannel inputChannel = new InputChannel(nettyChannel);
		inputChannel.setChannelApply(defaultChannelApply());
		InputChannelStateListener listener = new InputChannelStateListener(bootstrapService);
		inputChannel.setChannelStateListener(listener);
		inputChannel.active();
		return inputChannel;
	}

	public OutputChannel newOutputChannel(String group, String version, InetSocketAddress remoteAddress) {
		OutputChannel outputChannel = new OutputChannel(group, version, remoteAddress);
		outputChannel.setChannelApply(defaultChannelApply());
		OutputChannelStateListener listener = new OutputChannelStateListener(bootstrapService);
		outputChannel.setChannelStateListener(listener);
		return outputChannel;
	}

	public HttpChannel newHttpChannel(Channel nettyChannel) {
		HttpChannel httpChannel = new HttpChannel(actuatorDispatcher, providerDispatcher);
		httpChannel.setNettyChannel(nettyChannel);
		httpChannel.setChannelStateListener(new ChannelStateListener<HttpChannel>());
		return httpChannel;
	}

	private ChannelApply defaultChannelApply() {
		ChannelApply channelApply = new ChannelApply();
		channelApply.register(shakeApply);
		channelApply.register(eventApply);
		return channelApply;
	}
}
