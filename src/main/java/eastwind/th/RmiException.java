package eastwind.th;

public class RmiException extends EastWindException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8885519665304794741L;

	public RmiException() {
		
	}
	
	public RmiException(String message) {
		super(message);
	}
	
	public RmiException(Throwable cause) {
		super(cause);
	}
	
}
