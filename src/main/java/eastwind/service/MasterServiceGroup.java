package eastwind.service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.model.ElectionState;
import eastwind.model.ServerLite;
import eastwind.model.Vote;
import eastwind.rmi.RuleFactory;
import eastwind.support.DelayedExecutor;
import eastwind.support.DelayedTask;

public class MasterServiceGroup extends ServiceGroup {

	private static Logger LOGGER = LoggerFactory.getLogger(MasterServiceGroup.class);

	private int defaultCheckTime = 1000;
	private int defaultDelay = 100;

	private int term;
	// 1,follower 2,candidate 3,leader
	private int role;
	// 0,initial 1,electing 2,elected
	private int electState;
	private Service myself;
	private ElectionPolicy electionPolicy;
	private Service expectablyVote;
	private Service actuallyVote;
	private Map<Service, Service> quorum = new HashMap<>();
//	private Set<Service> clear = new HashSet<>();
	private long electTime;
	private CompletableFuture<Void> localElectFuture = new CompletableFuture<Void>();
	private CompletableFuture<Void> GroupElectFuture = new CompletableFuture<Void>();
	private DelayedExecutor executor;

	public MasterServiceGroup(Service myself, DelayedExecutor executor, ElectionPolicy electionPolicy,
			RuleFactory ruleFactory, ServiceOpener serviceOpener) {
		super(myself.getGroup(), myself.getVersion(), ruleFactory, serviceOpener);
		this.myself = myself;
		this.executor = executor;
		this.electionPolicy = electionPolicy;
	}

	@Override
	public synchronized void online(ChannelService service) {
		super.online(service);
		if (leader == null && electState == 0 && isHalfOnline()) {
			lookForLead();
		}
		if (service == expectablyVote && actuallyVote == null) {
			sendVote10(service);
		}
	}

	public void lookForLead() {
		electState = 1;
		CompletableFuture<Void> cf = waitForAll();
		cf.thenAccept(v -> {
			LOGGER.info("begin to look for leader...");
			if (leader == null && isHalfOnline()) {
				Service service = elect();
				LOGGER.info("vote to: {}", service);
				localElectFuture.complete(null);
				if (service == myself) {
					electTime = System.currentTimeMillis();
					Vote broadVote = new Vote();
					broadVote.type = 11;
					broadVote.leaderUuid = service.uuid;
					broadVote.leaderAddress = service.address;
					for (ChannelService s : onlines) {
						s.send(broadVote);
					}
					DelayedTask leaderElectTask = new DelayedTask();
					leaderElectTask.setDelay(defaultDelay);
					leaderElectTask.setConsumer(new CandidateTask());
					executor.delayExecute(leaderElectTask);
				} else {
					sendVote10((ChannelService) service);
				}
			} else {
				electState = 0;
			}
		});
	}

	public CompletableFuture<Void> waitForElectAndAll() {
		return CompletableFuture.allOf(GroupElectFuture, waitForAll());
	}

	private class CandidateTask implements Consumer<DelayedExecutor> {

		@Override
		public void accept(DelayedExecutor t) {
			Set<ChannelService> candidates = new HashSet<>();
			for (Entry<Service, Service> en : quorum.entrySet()) {
				if (en.getValue() != myself) {
					candidates.add((ChannelService) en.getValue());
				}
			}
			if (candidates.size() > 0) {
				for (ChannelService service : candidates) {
					if (service.isInitial()) {
						continueTask(t);
						return;
					}
				}
				Service leader = myself;
				for (ChannelService service : candidates) {
					if (electionPolicy.compare(service, myself) < 0) {
						leader = service;
					}
				}
				if (leader != myself) {
					LOGGER.info("new leader {}", leader);
					sendVote10((ChannelService) leader);

					Vote vote = new Vote();
					vote.type = 22;
					vote.leaderUuid = leader.uuid;
					vote.leaderAddress = leader.address;

					for (Entry<Service, Service> en : quorum.entrySet()) {
						if (en.getValue() == myself) {
							((ChannelService) en.getKey()).send(vote);
						}
					}
				} else {
					continueTask(t);
				}
			} else {
				if (System.currentTimeMillis() - electTime > defaultCheckTime) {
					LOGGER.info("i am leader.");
					role = 3;
					electState = 3;
					Vote vote = new Vote();
					vote.type = 30;
					List<ServerLite> servers = new ArrayList<>();
					vote.data = servers;
					for (Service service : quorum.keySet()) {
						ServerLite serverLite = new ServerLite();
						serverLite.uuid = service.uuid;
						serverLite.address = service.address;
						servers.add(serverLite);
					}
					for (Service service : quorum.keySet()) {
						((ChannelService) service).send(vote);
					}
				} else {
					continueTask(t);
				}
			}
		}

		private void continueTask(DelayedExecutor t) {
			DelayedTask leaderElectTask = new DelayedTask();
			leaderElectTask.setDelay(defaultDelay);
			leaderElectTask.setConsumer(this);
			t.delayExecute(leaderElectTask);
		}

	}

	public ChannelService getOrOpen(String uuid, InetSocketAddress address) {
		ChannelService service = getService(uuid);
		if (service == null) {
			service = getService(address);
			if (service == null) {
				service = open(address);
			}
		} else {
			serviceOpener.open(service);
		}
		return service;
	}

	private void sendVote10(ChannelService service) {
		Vote vote = new Vote();
		vote.type = 10;
		// vote to candidate
		CompletableFuture<ExchangeContext> vtcf = service.exchange(vote);
		vtcf.thenAccept(ec -> {
			Vote back = (Vote) ec.getResult();
			if (back.type == 99) { // accept
				this.actuallyVote = expectablyVote;
				Vote broadVote = new Vote();
				broadVote.type = 11;
				broadVote.leaderUuid = service.uuid;
				broadVote.leaderAddress = service.address;
				for (ChannelService s : onlines) {
					s.transfer(broadVote);
				}
				// tell leader other candidates
				for (Entry<Service, Service> en : quorum.entrySet()) {
					if (en.getValue() != actuallyVote) {
						Vote v = new Vote();
						v.type = 20;
						ChannelService candidate = (ChannelService) en.getValue();
						v.leaderUuid = candidate.uuid;
						v.leaderAddress = candidate.address;
						((ChannelService) actuallyVote).send(v);
					}
				}
			} else if (back.type == 12) {// vote to another
				if (myself.getUuid().equals(back.leaderUuid)) {
					quorum.put(service, myself);
				} else {
					ChannelService transfer = getOrOpen(back.leaderUuid, back.leaderAddress);
					this.expectablyVote = transfer;
					this.actuallyVote = null;
					if (transfer.isOnline()) {
						sendVote10(transfer);
					}
				}
			}
		});
	}

	public Service getExpectablyVote() {
		return expectablyVote;
	}

	public Service getActuallyVote() {
		return actuallyVote;
	}

	public boolean isVoteToMyself() {
		return actuallyVote == myself;
	}

	public void recvVote(Service from, Service to) {
		quorum.put(from, to);
		if (!isVoteToMyself()) {
			if (actuallyVote != null && actuallyVote != to) {
				Vote vote = new Vote();
				vote.type = 20;
				vote.leaderUuid = to.uuid;
				vote.leaderAddress = to.address;
				((ChannelService) actuallyVote).transfer(vote);
			}
		}
	}

	public void vote22(ChannelService service) {
		this.expectablyVote = service;
		this.actuallyVote = null;
		sendVote10(service);
	}

	public Service elect() {
		Service expect = myself;
		for (ChannelService service : onlines) {
			if (electionPolicy.compare(service, expect) < 0) {
				expect = service;
			}
		}
		this.expectablyVote = expect;
		if (expect == myself) {
			this.actuallyVote = myself;
		}
		return expect;
	}

	public Service getMyself() {
		return myself;
	}

	private boolean isHalfOnline() {
		int all = serviceMapper.getAll().size() + 1;
		int onlines = this.onlines.size() + 1;
		return onlines > all - onlines;
	}

	public CompletableFuture<Void> getLocalElectFuture() {
		return localElectFuture;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public int getElectState() {
		return electState;
	}

	public void setElectState(int electState) {
		this.electState = electState;
		if (electState == 3) {
			this.GroupElectFuture.complete(null);
		}
	}

	public ElectionState getElectionState() {
		ElectionState es = new ElectionState();
		es.term = term;
		es.role = role;
		es.state = electState;
		if (actuallyVote != null) {
			es.leaderUuid = actuallyVote.uuid;
			es.leaderAddress = actuallyVote.address;
		}
		return es;
	}
}
