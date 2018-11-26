package eastwind.th;

public class InvalidRemoteServiceException extends EastWindException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8667260936323853671L;

	public InvalidRemoteServiceException(String group, String version) {
		super(group + "-" + version);
	}
}
