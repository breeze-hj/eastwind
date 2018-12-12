package eastwind.apply;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.EastWindApplication;
import eastwind.channel.ExchangePair;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TcpChannel;
import eastwind.model.Convert;
import eastwind.model.ElectionState;
import eastwind.model.Shake;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;
import eastwind.rmi.RMDRegistry;
import eastwind.service.BootstrapService;
import eastwind.service.BootstrapServiceable;
import eastwind.service.ChannelService;
import eastwind.service.MasterServiceGroup;
import eastwind.service.ServiceGroup;

public class ShakeApply extends BootstrapServiceable implements Apply<Shake> {

	private static Logger LOGGER = LoggerFactory.getLogger(ShakeApply.class);

	public ShakeApply(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Shake shake, ExchangePair exchangePair) {
		String group = shake.group;
		String version = shake.version;
		String uuid = shake.uuid;
		InetSocketAddress address = shake.address;

		inputChannel.setGroup(group);
		inputChannel.setVersion(version);

		ServiceGroup serviceGroup = bootstrapService.getServiceGroup(group, version);
		ChannelService service = serviceGroup.map(shake.address, uuid);
		service.addChannel(inputChannel);
		service.setPropertys(shake.properties);
		service.setOthers(shake.others);
		service.setElectionState(shake.electionState);

		inputChannel.bindTo(service);
		setupApply(inputChannel, service);
		inputChannel.getChannelApply().register(new RmdApply(bootstrapService.getRMdRegistry()));

		inputChannel.shake();
		if (bootstrapService.isTeamate(group, version) && !bootstrapService.isMySelf(uuid)) {
			bootstrapService.getMasterServiceGroup().getOrOpen(uuid, address);
		}

		return bootstrapService.shakeBuilder().build(true);
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Shake shake, ExchangePair exchangePair) {
		String group = shake.group;
		String version = shake.version;
		String uuid = shake.uuid;
		InetSocketAddress address = shake.address;

		ServiceGroup serviceGroup = bootstrapService.getServiceGroup(group, version);
		ChannelService service = serviceGroup.map(address, uuid);

		service.setStartTime(shake.startTime);
		service.setAddress(shake.address);
		service.setPropertys(shake.properties);
		service.setOthers(shake.others);
		service.setElectionState(shake.electionState);

		setupApply(outputChannel, service);
		outputChannel.getChannelApply().register(new RmdApply(service.getRMDAssign()));
		outputChannel.shake();

		if (bootstrapService.isTeamate(group, version) && !bootstrapService.isMySelf(uuid)) {
			MasterServiceGroup masterServiceGroup = bootstrapService.getMasterServiceGroup();
			ElectionState es = shake.electionState;
			if (es.state == 1) {
				if (es.leaderUuid != null) {
					if (bootstrapService.isMySelf(es.leaderUuid)) {
						masterServiceGroup.recvVote(service, masterServiceGroup.getMyself());
					} else {

						ChannelService candidate = masterServiceGroup.getOrOpen(es.leaderUuid, es.leaderAddress);
						masterServiceGroup.recvVote(service, candidate);
					}
				}
			} else if (es.state == 2) {

			}
		}

		return null;
	}

	private ResponseApply responseApply = new ResponseApply();
	private RedirectApply redirectApply = new RedirectApply();
	private TransferAckApply transferAckApply = new TransferAckApply();

	private void setupApply(TcpChannel tcpChannel, ChannelService service) {
		ChannelApply channelApply = tcpChannel.getChannelApply();

		EastWindApplication eastWindApplication = bootstrapService.getEastWindApplication();
		RMDRegistry rmdRegistry = bootstrapService.getRMdRegistry();
		ExecutorService customerExecutor = bootstrapService.getCustomerExecutor();
		RequestApply requestApply = new RequestApply(eastWindApplication, rmdRegistry, customerExecutor);
		channelApply.register(requestApply);

		channelApply.register(responseApply);
		channelApply.register(redirectApply);
		channelApply.register(transferAckApply);

		VoteApply voteApply = new VoteApply(bootstrapService.getMasterServiceGroup());
		channelApply.register(voteApply);
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
