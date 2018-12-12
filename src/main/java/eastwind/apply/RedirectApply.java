package eastwind.apply;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.ExchangePair;
import eastwind.model.Convert;
import eastwind.model.Redirect;

public class RedirectApply implements Apply<Redirect> {

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Redirect t, ExchangePair exchangePair) {
		return null;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Redirect t, ExchangePair exchangePair) {
		completeExchange(outputChannel, t, exchangePair.respondTo);
		return null;
	}

	@Override
	public Convert<Redirect> getConvert() {
		return Convert.DEFAULT();
	}

}
