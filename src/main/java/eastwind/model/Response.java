package eastwind.model;

public class Response {

	public static final byte SUCCESS = 1;
	public static final byte FAILED = 2;
	public static final byte CANCELED = 3;

	public byte state;
	public transient Object value;
	
	public static Response success(Object value) {
		Response response = new Response();
		response.state = SUCCESS;
		response.value = value;
		return response;
	}

	public static Response fail(Throwable th) {
		Response response = new Response();
		response.state = FAILED;
		response.value = th;
		return response;
	}
	
}
