package eastwind.th;

public class EastWindException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1912095830021079992L;

	public EastWindException() {
		
	}
	
	public EastWindException(String message) {
		super(message);
	}
	
	public EastWindException(Throwable cause) {
		super(cause);
	}
}
