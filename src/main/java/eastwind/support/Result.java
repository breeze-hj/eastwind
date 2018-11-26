package eastwind.support;

public class Result<T> {

	private static final byte SUCCESS = 1;
	private static final byte FAILED = 2;
	private static final byte CANCELED = 3;

	private byte state;
	private transient T value;
	private transient Throwable th;

	public static <T> Result<T> success(T value) {
		Result<T> result = new Result<>();
		result.state = SUCCESS;
		result.value = value;
		return result;
	}

	public static <T> Result<T> fail(Throwable th) {
		Result<T> result = new Result<>();
		result.state = FAILED;
		result.th = th;
		return result;
	}

	public boolean isCanceled() {
		return state == CANCELED;
	}

	public boolean isSuccess() {
		return state == SUCCESS;
	}

	public boolean isFailed() {
		return state == FAILED;
	}

	public T getValue() {
		return value;
	}

	public Throwable getTh() {
		return th;
	}

}
