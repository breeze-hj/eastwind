package eastwind.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * Created by jan.huang on 2017/10/16.
 */
public interface AsyncOpenChannel {

	default void bind(ChannelFuture future) {
		setNettyChannel(future.channel());
		future.addListener(cf -> {
			if (cf.isSuccess()) {
				active();
			} else {
				inactive(cf.cause());
			}
		});
	}

	void setNettyChannel(Channel channel);

	void active();

	void inactive(Throwable th);
}
