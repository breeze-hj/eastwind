package eastwind.apply;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.ExchangePair;
import eastwind.model.Convert;
import eastwind.model.RMD;
import eastwind.rmi.RMDAssign;

public class RmdApply implements Apply<RMD> {

	private RMDAssign rmdAssign;

	public RmdApply(RMDAssign rmdAssign) {
		this.rmdAssign = rmdAssign;
	}

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, RMD t, ExchangePair exchangePair) {
		RMD matched = rmdAssign.assignFrom(t);
		if (matched == null) {
			t.supply = false;
			return t;
		} else {
			return matched;
		}
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, RMD t, ExchangePair exchangePair) {
		rmdAssign.put(t);
		completeExchange(outputChannel, t, exchangePair.respondTo);
		return null;
	}

	@Override
	public Convert<RMD> getConvert() {
		return Convert.DEFAULT();
	}

}
