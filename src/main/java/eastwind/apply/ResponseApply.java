package eastwind.apply;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.ExchangePair;
import eastwind.model.Convert;
import eastwind.model.Response;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;

public class ResponseApply implements Apply<Response> {

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Response t,  ExchangePair exchangePair) {
		return null;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Response t, ExchangePair exchangePair) {
		completeExchange(outputChannel, t, exchangePair.respondTo);
		return null;
	}

	@Override
	public Convert<Response> getConvert() {
		return new Convert<Response>() {
			@Override
			public void build(Response t, TcpObjectBuilder builder) {
				builder.body(t.value);
			}

			@Override
			public void init(TcpObject tcpObject, Response t) {
				t.value = tcpObject.body;
			}
			
		};
	}
	
	
	
}
