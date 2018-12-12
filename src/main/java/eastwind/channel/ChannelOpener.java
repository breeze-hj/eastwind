package eastwind.channel;

import eastwind.codec.ChannelClassifyHandler;
import eastwind.codec.IOExceptionHandler;
import eastwind.codec.RecvHandler;
import eastwind.codec.TcpObjectCodec;
import eastwind.codec.TransferSegmentHandler;
import eastwind.support.EWUtils;
import eastwind.support.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChannelOpener {

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap = new ServerBootstrap();
	private NioEventLoopGroup parentGroup;
	private NioEventLoopGroup childGroup;

	public ChannelOpener(String threadPrefix) {
		parentGroup = new NioEventLoopGroup(1, new NamedThreadFactory(threadPrefix + "-io0"));
		childGroup = new NioEventLoopGroup(0, new NamedThreadFactory(threadPrefix + "-io1"));
		
		IOExceptionHandler handler = new IOExceptionHandler();
		ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(EWUtils.getSimpleName(IOExceptionHandler.class), handler);
				pipeline.addLast(EWUtils.getSimpleName(TcpObjectCodec.class), new TcpObjectCodec());
				pipeline.addLast(EWUtils.getSimpleName(TransferSegmentHandler.class), new TransferSegmentHandler());
				pipeline.addLast(EWUtils.getSimpleName(RecvHandler.class), new RecvHandler());
			}
		};
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(childGroup);
		bootstrap.handler(channelInitializer);
	}

	public void open(MasterChannel masterChannel) {
		ChannelClassifyHandler cch = new ChannelClassifyHandler(masterChannel.getChildChannelFactory());
		ChannelInitializer<Channel> sci = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(cch);
			}
		};

		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.group(parentGroup, childGroup);
		serverBootstrap.childHandler(sci);
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

		masterChannel.bind(serverBootstrap.bind(masterChannel.getAddress()));
	}

	public void open(OutputChannel outputChannel) {
		outputChannel.incrementOpenMod();
		outputChannel.setOpening(true);
		ChannelFuture channelFuture = bootstrap.connect(outputChannel.getRemoteAddress());
		channelFuture.addListener((f) -> NettyChannelBinder.bind(channelFuture.channel(), outputChannel));
		outputChannel.bind(channelFuture);
	}

	public void shutdown() {
		parentGroup.shutdownGracefully();
		childGroup.shutdownGracefully();
	}
}
