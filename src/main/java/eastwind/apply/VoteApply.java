package eastwind.apply;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.channel.ExchangePair;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TcpChannel;
import eastwind.model.Convert;
import eastwind.model.ServerLite;
import eastwind.model.Vote;
import eastwind.service.ChannelService;
import eastwind.service.MasterServiceGroup;
import eastwind.service.Service;

public class VoteApply implements Apply<Vote> {

	private static Logger LOGGER = LoggerFactory.getLogger(VoteApply.class);

	private MasterServiceGroup serviceGroup;

	public VoteApply(MasterServiceGroup serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Vote t, ExchangePair exchangePair) {
		ChannelService remote = inputChannel.getService();
		if (t.type == 10) { // vote
			CompletableFuture<Void> cf = serviceGroup.getLocalElectFuture();
			if (cf.isDone()) {
				return vote11(remote);
			} else {
				cf.thenAccept(v -> {
					Object back = vote11(remote);
					inputChannel.respond(exchangePair.id, back);
				});
				return null;
			}
		}

		if (t.type == 30) {
			serviceGroup.setRole(1);
			serviceGroup.setElectState(3);
			@SuppressWarnings("unchecked")
			List<ServerLite> l = (List<ServerLite>) t.data;
			for (ServerLite serverLite : l) {
				if (!isMyself(serverLite.uuid)) {
					serviceGroup.getOrOpen(serverLite.uuid, serverLite.address);
				}
			}
		}

		if (!isMyself(t.leaderUuid)) {
			if (t.type == 11) {
				ChannelService voteTo = serviceGroup.getOrOpen(t.leaderUuid, t.leaderAddress);
				serviceGroup.recvVote(remote, voteTo);
			} else if (t.type == 20) {
				serviceGroup.getOrOpen(t.leaderUuid, t.leaderAddress);
			} else if (t.type == 22) {
				LOGGER.info("change vote to: {}", remote);
				vote22(inputChannel, t);
			}
		}
		return null;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Vote t, ExchangePair exchangePair) {
		if (t.type == 99 || t.type == 12) { // accept || vote another
			if (t.type == 12) {
				LOGGER.info("{} advise to vote to: {}", outputChannel.getService(), t.leaderAddress);
			}
			completeExchange(outputChannel, t, exchangePair.respondTo);
		}
		return null;
	}

	// candidate 转移
	public void vote22(TcpChannel channel, Vote t) {
		ChannelService service = serviceGroup.getOrOpen(t.leaderUuid, t.leaderAddress);
		serviceGroup.recvVote(channel.getService(), service);
		serviceGroup.vote22(service);
	}

	private boolean isMyself(String uuid) {
		return serviceGroup.getMyself().getUuid().equals(uuid);
	}

	private Object vote11(ChannelService remote) {
		Service voteTo = serviceGroup.getActuallyVote();
		if (serviceGroup.isVoteToMyself()) { // accept
			serviceGroup.recvVote(remote, voteTo);
			Vote back = new Vote();
			back.type = 99;
			return back;
		} else { // please vote another
			if (voteTo == null) {
				voteTo = serviceGroup.getExpectablyVote();
			}
			Vote back = new Vote();
			back.type = 12;
			back.leaderUuid = voteTo.getUuid();
			back.leaderAddress = voteTo.getAddress();
			return back;
		}
	}

	@Override
	public Convert<Vote> getConvert() {
		return Convert.DEFAULT();
	}

}
