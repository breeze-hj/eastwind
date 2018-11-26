package eastwind.apply;

import eastwind.Application;
import eastwind.EastWindApplication;
import eastwind.EventBus;
import eastwind.channel.InputChannel;
import eastwind.channel.OutputChannel;
import eastwind.channel.TransferContext;
import eastwind.model.Convert;
import eastwind.model.Event;
import eastwind.model.TcpObject;
import eastwind.model.TcpObjectBuilder;
import eastwind.service.DefaultEventBus;
import eastwind.service.EventBusManager;

public class EventApply implements Apply<Event> {

	private EastWindApplication master;
	private EventBusManager eventBusManager;

	public EventApply(EastWindApplication master, EventBusManager eventBusManager) {
		this.master = master;
		this.eventBusManager = eventBusManager;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object applyFromInputChannel(InputChannel inputChannel, Event t, TransferContext transferContext) {
		EventBus<Object> eventBus = eventBusManager.get(t.name);
		Application remote = inputChannel.getService().getApplication();
		((DefaultEventBus) eventBus).getConsumer().accept(t.data, master, remote);
		return null;
	}

	@Override
	public Object applyFromOutputChannel(OutputChannel outputChannel, Event t, TransferContext transferContext) {
		return null;
	}

	@Override
	public Convert<Event> getConvert() {
		return new Convert<Event>() {
			@Override
			public void build(Event t, TcpObjectBuilder builder) {
				builder.body(t.data);
			}

			@Override
			public void init(TcpObject tcpObject, Event t) {
				t.data = tcpObject.body;
			}
		};
	}

}
