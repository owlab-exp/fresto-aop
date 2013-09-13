package fresto.aspects;

import java.util.logging.Logger;

public class FrestoTracker {
	private static Logger LOGGER = Logger.getLogger("FrestoTracker");

	private static final ThreadLocal<FrestoContext> threadLocal = new ThreadLocal<FrestoContext>();

	private static boolean contextExists = false;

	public static void set(FrestoContext fc) {
	    threadLocal.set(fc);
	    contextExists = true;
	    LOGGER.info("Set fresto context");
	}

	public static void unset() {
	    threadLocal.remove();
	    contextExists = false;
	    LOGGER.info("Unset fresto context");
	}

	public static FrestoContext get() {
	    if(!contextExists) {
		LOGGER.severe("FrestoCotnext does not exist");
		return null;
	    }
	    LOGGER.info("Get fresto context");
	    return threadLocal.get();
	}


	//Fcacade
	public static void createFrestoContext(String uuid) {
	    FrestoContext fc = FrestoContext.createInstance(uuid);
	    FrestoTracker.set(fc);
	}

	public static void createFrestoContext() {
	    FrestoContext fc = FrestoContext.createInstance();
	    FrestoTracker.set(fc);
	}

	public static void beginTrack() {
	    if(!contextExists) {
		LOGGER.severe("FrestoCotnext does not exist");
		return;
	    }
	    FrestoTracker.get().increaseDepth();
	}

	public static void endTrack() {
	    if(!contextExists) {
		LOGGER.severe("FrestoCotnext does not exist");
		return;
	    }
	    int depth = FrestoTracker.get().decreaseDepth();
	    if(depth == 0) {
		FrestoTracker.unset();
	    }
	}
}
    
