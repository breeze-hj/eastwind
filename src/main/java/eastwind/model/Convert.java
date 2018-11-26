package eastwind.model;

public interface Convert<T> {

	public static <T> Convert<T> DEFAULT() {
		return new Convert<T>() {
			@Override
			public void build(T t, TcpObjectBuilder builder) {
			}

			@Override
			public void init(TcpObject tcpObject, T t) {
			}
		};
	}

	default TcpObject to(T t) {
		TcpObjectBuilder builder = TcpObjectBuilder.newExtBuilder(t);
		build(t, builder);
		return builder.build();
	}

	void build(T t, TcpObjectBuilder builder);

	default T from(TcpObject tcpObject) {
		@SuppressWarnings("unchecked")
		T t = (T) tcpObject.ext;
		init(tcpObject, t);
		return t;
	}

	void init(TcpObject tcpObject, T t);

}
