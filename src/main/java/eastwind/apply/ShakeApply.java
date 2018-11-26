package eastwind.apply;

import java.util.concurrent.ExecutorService;

import eastwind.EastWindApplication;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TcpChannel;
import eastwind.channel.TransferContext;
import eastwind.model.Convert;
import eastwind.model.RMD;
import eastwind.model.Redirect;
import eastwind.model.Request;
import eastwind.model.Response;
import eastwind.model.Shake;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;
import eastwind.rmi.RMDRegistry;
import eastwind.service.BootstrapService;
import eastwind.service.BootstrapServiceable;
import eastwind.service.ChannelService;
import eastwind.service.ServiceGroup;

public class ShakeApply extends BootstrapServiceable implements Apply<Shake> {

	public ShakeApply(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Shake shake, TransferContext transferContext) {
		String group = shake.group;
		String version = shake.version;
		String uuid = shake.uuid;

		inputChannel.setGroup(group);
		inputChannel.setVersion(version);

		ServiceGroup serviceGroup = bootstrapService.getServiceGroup(group, version);
		ChannelService service = serviceGroup.map(shake.address, uuid);
		service.setPropertys(shake.properties);
		service.addChannel(inputChannel);
		inputChannel.bindTo(service);
		registerApply(inputChannel, service);
		inputChannel.getChannelApply().register(RMD.class, new RmdApply(bootstrapService.getRMdRegistry()));
		inputChannel.shake();

		return bootstrapService.shakeBuilder().build(true);
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Shake shake, TransferContext transferContext) {
		ServiceGroup serviceGroup = bootstrapService.getServiceGroup(shake.group, shake.version);
		ChannelService service = serviceGroup.map(shake.address, shake.uuid);
		service.setStartTime(shake.startTime);
		service.setAddress(shake.address);
		service.setPropertys(shake.properties);
		registerApply(outputChannel, service);
		outputChannel.getChannelApply().register(RMD.class, new RmdApply(service.getRMDAssign()));
		outputChannel.shake();
		return null;
	}

	private ResponseApply responseApply = new ResponseApply();
	private RedirectApply redirectApply = new RedirectApply();

	private void registerApply(TcpChannel tcpChannel, ChannelService service) {
		ChannelApply channelApply = tcpChannel.getChannelApply();

		EastWindApplication eastWindApplication = bootstrapService.getEastWindApplication();
		RMDRegistry rmdRegistry = bootstrapService.getRMdRegistry();
		ExecutorService customerExecutor = bootstrapService.getCustomerExecutor();
		RequestApply requestApply = new RequestApply(eastWindApplication, rmdRegistry, customerExecutor);
		channelApply.register(Request.class, requestApply);

		channelApply.register(Response.class, responseApply);
		channelApply.register(Redirect.class, redirectApply);
	}

	@Override
	public Convert<Shake> getConvert() {
		return new Convert<Shake>() {

			@Override
			public void build(Shake t, TcpObjectBuilder builder) {
				builder.header(t.properties);
			}

			@Override
			public void init(TcpObject tcpObject, Shake t) {
				t.properties = tcpObject.header;
			}
		};
	}

}
