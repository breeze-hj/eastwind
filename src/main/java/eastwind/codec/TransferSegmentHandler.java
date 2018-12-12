package eastwind.codec;

import java.util.Iterator;

import eastwind.model.TcpObject;
import eastwind.model.TransferSegment;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

@Sharable
public class TransferSegmentHandler extends ChannelOutboundHandlerAdapter {

	private static final int WINDOW_SIZE = 7 * 1024;
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof TransferSegment) {
			TransferSegment transferSegment = (TransferSegment) msg;
			Iterator<TcpObject> it = transferSegment.it;
			int size = 0;
			int bytes = 0;
			TcpObject last = null;
			while (it.hasNext() && bytes < WINDOW_SIZE) {
				size++;
				last = it.next();
				ctx.write(last);
				bytes += last.bytes;
			}
			if (last != null) {
				transferSegment.lastId = last.id;
				ctx.writeAndFlush(transferSegment.createAckObject());
			}
			transferSegment.size = size;
			transferSegment.bytes = bytes;
			promise.setSuccess();
		} else {
			super.write(ctx, msg, promise);
		}
	}

}
