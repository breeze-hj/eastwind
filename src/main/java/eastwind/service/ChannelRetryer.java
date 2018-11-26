package eastwind.service;

import eastwind.channel.ChannelOpener;
import eastwind.channel.OutputChannel;
import eastwind.support.DelayedExecutor;
import eastwind.support.DelayedTask;

public class ChannelRetryer {

	private ChannelOpener channelOpener;
	private DelayedExecutor delayedExecutor;

	public ChannelRetryer(ChannelOpener channelOpener, DelayedExecutor delayedExecutor) {
		this.channelOpener = channelOpener;
		this.delayedExecutor = delayedExecutor;
	}

	public void retry(OutputChannel outputChannel) {
		int retrys = outputChannel.getRetrys();
		if (retrys > 7) {
			retrys = 7;
		}
		long delay = (retrys * 2 + 1) * 1000;
		DelayedTask delayedTask = new DelayedTask();
		delayedTask.setDelay(delay);
		delayedTask.setConsumer(exe -> {
			channelOpener.open(outputChannel);
		});
		outputChannel.incrementRetrys();
		delayedExecutor.delayExecute(delayedTask);
	}

}
