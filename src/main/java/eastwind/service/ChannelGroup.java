package eastwind.service;

import java.util.ArrayList;
import java.util.List;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TcpChannel;

public class ChannelGroup {
	
	private List<OutputChannel> outputChannels = new ArrayList<>();
	private List<InputChannel> inputChannels = new ArrayList<>();
	
	public void addChannel(TcpChannel tcpChannel) {
		if (tcpChannel instanceof InputChannel) {
			inputChannels.add((InputChannel) tcpChannel);
		} else if (tcpChannel instanceof OutputChannel) {
			outputChannels.add((OutputChannel) tcpChannel);
		}
	}

	public void removeChannel(TcpChannel tcpChannel) {
		if (tcpChannel instanceof InputChannel) {
			inputChannels.remove((InputChannel) tcpChannel);
		} else {
			outputChannels.remove((OutputChannel) tcpChannel);
		}
	}

	public List<OutputChannel> getOutputChannels() {
		return outputChannels;
	}
	
	public OutputChannel getOne() {
		return outputChannels.get(0);
	}
}
