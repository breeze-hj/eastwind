package eastwind.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.http.HttpRequestDispatcher;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpChannel extends ReceivableChannel<FullHttpRequest> {

	private static Logger LOGGER = LoggerFactory.getLogger(HttpChannel.class);

	private HttpRequestDispatcher actuatorDispatcher;
	private HttpRequestDispatcher providerDispatcher;

	public HttpChannel(HttpRequestDispatcher actuatorDispatcher, HttpRequestDispatcher providerDispatcher) {
		this.actuatorDispatcher = actuatorDispatcher;
		this.providerDispatcher = providerDispatcher;
	}

	@Override
	public void recv(FullHttpRequest request) {
		QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
		String path = decoder.path();
		if (path.equals("/favicon.ico")) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
			getNettyChannel().writeAndFlush(response);
		} else {
			LOGGER.info("handle HttpRequest:{}", request.uri());
			FullHttpResponse response = null;
			try {
				if (path.equals("/") || path.startsWith("/_")) {
					response = actuatorDispatcher.forward(request);
				} else {
					response = providerDispatcher.forward(request);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (response != null) {
				getNettyChannel().writeAndFlush(response);
			}
		}
	}
}
