package eastwind.apply;

import eastwind.channel.ExchangePair;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.model.Convert;
import eastwind.model.TransferAck;
import eastwind.service.ChannelService;

public class TransferAckApply implements Apply<TransferAck> {

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, TransferAck t, ExchangePair exchangePair) {
		return t;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, TransferAck t, ExchangePair exchangePair) {
		ChannelService service = outputChannel.getService();
		service.getTransferBuffer().ack(t.id);
		return null;
	}

	@Override
	public Convert<TransferAck> getConvert() {
		return Convert.DEFAULT();
	}

}
