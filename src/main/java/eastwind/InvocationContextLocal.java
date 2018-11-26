package eastwind;

public class InvocationContextLocal {

	static ThreadLocal<InvocationContext<?>> TL = new ThreadLocal<>();
	
	public static InvocationContext<?> get() {
		return TL.get();
	}
	
	public static void set(InvocationContext<?> context) {
		TL.set(context);
	}
}
