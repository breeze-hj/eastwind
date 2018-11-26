package eastwind.channel;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * Created by jan.huang on 2018/3/5.
 */
public class NettyChannelBinder {

    private static final AttributeKey<ReceivableChannel<?>> TCP_CHANNEL = AttributeKey.valueOf("TCP_CHANNEL");

    public static void bind(Channel channel, ReceivableChannel<?> transferChannel) {
        channel.attr(TCP_CHANNEL).set(transferChannel);
    }

    public static ReceivableChannel<?> getBinder(Channel channel) {
        return channel.attr(TCP_CHANNEL).get();
    }
}
