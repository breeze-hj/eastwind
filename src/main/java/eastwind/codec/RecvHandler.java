package eastwind.codec;

import io.netty.channel.ChannelHandler.Sharable;
import eastwind.channel.NettyChannelBinder;
import eastwind.channel.ReceivableChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class RecvHandler extends SimpleChannelInboundHandler<Object> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		ReceivableChannel channel = NettyChannelBinder.getBinder(ctx.channel());
		channel.recv(msg);
	}

}
