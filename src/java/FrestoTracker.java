package fresto.aspects;

public class FrestoTracker {
	private static final ThreadLocal<FrestoContext> threadLocal = new ThreadLocal<FrestoContext>();

	public static void set(FrestoContext fc) {
	    threadLocal.set(fc);
	}

	public static void unset() {
	    threadLocal.remove();
	}

	public static FrestoContext get() {
	    return threadLocal.get();
	}
}
    
