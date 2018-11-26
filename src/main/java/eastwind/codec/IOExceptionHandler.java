package eastwind.codec;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class IOExceptionHandler extends ChannelInboundHandlerAdapter {

	private static Logger LOGGER = LoggerFactory.getLogger(IOExceptionHandler.class);
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException) {
			LOGGER.warn("channel {}:{}", ctx.channel(), cause.toString());
		} else {
			super.exceptionCaught(ctx, cause);
		}
	}

}
