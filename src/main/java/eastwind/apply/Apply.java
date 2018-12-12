package eastwind.apply;

import eastwind.channel.ExchangePair;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.model.Convert;
import eastwind.service.ChannelService;
import eastwind.service.ExchangeContext;

public interface Apply<T> {

	Object applyFromInputChannel(InputChannel inputChannel, T t, ExchangePair exchangePair);

	Object applyFromOutputChannel(OutputChannel outputChannel, T t, ExchangePair exchangePair);

	default void completeExchange(OutputChannel outputChannel, T t, Long respondId) {
		ChannelService service = outputChannel.getService();
		ExchangeContext context = service.removeExchange(respondId);
		context.complete(t);
	}
	
	Convert<T> getConvert();
}
