package eastwind.codec;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eastwind.model.TcpObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class TcpObjectCodec extends ByteToMessageCodec<TcpObject> {

	private static Logger LOGGER = LoggerFactory.getLogger(TcpObjectCodec.class);

	private static final int MAX_WINDOW_SIZE = 7 * 1024;

	public static final byte[] MAGIC = new byte[6];
	static {
		MAGIC[0] = 0x00;
		System.arraycopy("jddf".getBytes(), 0, MAGIC, 1, 4);
		MAGIC[5] = (byte) 0xff;
	}

	private static ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(() -> KryoFactory.newKryo());
	private static ThreadLocal<Output> OUTPUT = ThreadLocal.withInitial(() -> new Output(4096));
	private static ThreadLocal<Input> INPUT = ThreadLocal.withInitial(() -> new Input(4096));

	@Override
	protected void encode(ChannelHandlerContext ctx, TcpObject msg, ByteBuf out) throws Exception {
		LOGGER.debug("-->{}:{}", ctx.channel().remoteAddress(), msg);
		
		Kryo kryo = KRYO.get();
		Output output = OUTPUT.get();

		if (msg.hasHeader) {
			writeObject(out, kryo, output, msg.header);
		}

		if (msg.args > 0) {
			Object[] data = null;
			if (msg.args == 1) {
				data = new Object[] { msg.body };
			} else {
				data = (Object[]) msg.body;
			}
			for (int j = 0; j < msg.args; j++) {
				writeObject(out, kryo, output, data[j]);
			}
		} else if (msg.args == -1) {
			Iterator<?> it = (Iterator<?>) msg.body;
			int i = 0;
			for (; it.hasNext() && (out.readableBytes() < MAX_WINDOW_SIZE || i == 0); i++) {
				writeObject(out, kryo, output, it.next());
			}
			msg.args = i;
		}

		ByteBuf line = ctx.alloc().buffer();
		line.writeBytes(MAGIC);
		line.writeMedium(0);
		int i = line.writerIndex();
		line.writeShort(0);
		output.setOutputStream(new ByteBufOutputStream(line));
		kryo.writeClassAndObject(output, msg);
		output.flush();
		line.setShort(i, line.writerIndex() - i);
		line.setMedium(MAGIC.length, line.readableBytes() - MAGIC.length + out.readableBytes());

		output.clear();
		msg.bytes = line.readableBytes() + out.readableBytes();
		ctx.write(line);
	}

	private void writeObject(ByteBuf out, Kryo kryo, Output output, Object data) {
		int i = out.writerIndex();
		out.writeMedium(0);
		output.setOutputStream(new ByteBufOutputStream(out));
		kryo.writeClassAndObject(output, data);
		output.flush();
		out.setMedium(i, out.writerIndex() - i);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < MAGIC.length + 3) {
			return;
		}
		in.markReaderIndex();
		in.skipBytes(MAGIC.length);
		int len = in.readMedium();
		if (in.readableBytes() < len - 3) {
			in.resetReaderIndex();
			return;
		}

		Kryo kryo = KRYO.get();
		Input input = INPUT.get();
		int i = in.readShort();
		input.setInputStream(new ByteBufInputStream(in, i - 2));
		TcpObject msg = (TcpObject) kryo.readClassAndObject(input);
		input.close();

		if (msg.hasHeader) {
			i = in.readMedium();
			input.setInputStream(new ByteBufInputStream(in, i - 3));
			@SuppressWarnings("unchecked")
			Map<Object, Object> header = (Map<Object, Object>) kryo.readClassAndObject(input);
			msg.header = header;
			input.close();
		}

		if (msg.args > 0) {
			Object[] data = new Object[msg.args];
			for (int j = 0; j < msg.args; j++) {
				i = in.readMedium();
				input.setInputStream(new ByteBufInputStream(in, i - 3));
				Object obj = kryo.readClassAndObject(input);
				data[j] = obj;
				input.close();
			}
			if (msg.args == 1) {
				msg.body = data[0];
			} else {
				msg.body = data;
			}
		}
		LOGGER.debug("{}-->:{}", ctx.channel().remoteAddress(), msg);
		msg.bytes = len + MAGIC.length;
		out.add(msg);
	}
}
