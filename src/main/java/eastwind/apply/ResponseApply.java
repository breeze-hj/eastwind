package eastwind.apply;

import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TransferContext;
import eastwind.model.Convert;
import eastwind.model.Response;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;

public class ResponseApply implements Apply<Response> {

	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Response t,  TransferContext transferContext) {
		return null;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Response t, TransferContext transferContext) {
		completeExchange(outputChannel, t, transferContext.respondTo);
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
