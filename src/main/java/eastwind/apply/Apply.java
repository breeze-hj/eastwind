package eastwind.apply;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TransferContext;
import eastwind.model.Convert;
import eastwind.service.ChannelService;
import eastwind.service.ExchangeContext;

public interface Apply<T> {

	Object applyFromInputChannel(InputChannel inputChannel, T t, TransferContext transferContext);

	Object applyFromOutputChannel(OutputChannel outputChannel, T t, TransferContext transferContext);

	default void completeExchange(OutputChannel outputChannel, T t, Long respondId) {
		ChannelService service = outputChannel.getService();
		ExchangeContext context = service.removeExchange(respondId);
		context.complete(t);
	}
	
	Convert<T> getConvert();
}
