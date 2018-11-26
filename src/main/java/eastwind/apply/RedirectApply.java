package eastwind.apply;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TransferContext;
import eastwind.model.Convert;
import eastwind.model.Redirect;

public class RedirectApply implements Apply<Redirect> {

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Redirect t, TransferContext transferContext) {
		return null;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Redirect t, TransferContext transferContext) {
		completeExchange(outputChannel, t, transferContext.respondTo);
		return null;
	}

	@Override
	public Convert<Redirect> getConvert() {
		return Convert.DEFAULT();
	}

}
