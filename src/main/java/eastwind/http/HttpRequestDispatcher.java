package eastwind.http;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import eastwind.model.RMD;
import eastwind.rmi.RMDRegistry;
import eastwind.rmi.RmiEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpRequestDispatcher {

	private RMDRegistry rmdRegistry;
	private ObjectMapper objectMapper;

	public HttpRequestDispatcher(RMDRegistry rmdRegistry, ObjectMapper objectMapper) {
		this.rmdRegistry = rmdRegistry;
		this.objectMapper = objectMapper;
	}
	
	public FullHttpResponse forward(FullHttpRequest request) throws Exception {
		URI uri = new URI(request.uri());
		QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
		String path = uri.getPath();
		if (path.equals("/")) {
			path = "/_info";
		}
		List<RMD> rmds = rmdRegistry.get(path);
		if (rmds.size() != 1) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
			return response;
		}
		RMD rmd = rmds.get(0);
		RmiEntity rmiEntity = rmdRegistry.getEntity(rmd.alias);
		HttpMethod httpMethod = request.method();
		if (httpMethod == HttpMethod.GET) {
			Method method = rmiEntity.getMethod();
			Class<?>[] paramTypes = method.getParameterTypes();
			Parameter[] pts = method.getParameters();
			
			Map<String, List<String>> params = decoder.parameters();
			Object[] args = new Object[pts.length];
			for (int i = 0; i < pts.length; i++) {
				String k = pts[i].getName();
				if (params.get(k) == null) {
					continue;
				}
				String v = params.get(k).get(0);
				Class<?> type = paramTypes[i];
				Object arg = objectMapper.readValue(v, type);
				args[i] = arg;
			}
			Object result = null;
			if (args.length == 0) {
				result = method.invoke(rmiEntity.getTarget());
			} else {
				result = method.invoke(rmiEntity.getTarget(), args);
			}
			String str = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
			ByteBuf buf = Unpooled.copiedBuffer(str, Charset.forName("utf-8"));
			
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
			return response;
		}
		return null;
	}

}
