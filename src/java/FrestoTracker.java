package fresto.aspects;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fresto.event.HttpRequestEvent;

public class FrestoTracker {
	private static Logger LOGGER = Logger.getLogger("FrestoTracker");

	private static final ThreadLocal<FrestoContext> threadLocal = new ThreadLocal<FrestoContext>();

	private static boolean frestoContextExists = false;


	public static void set(FrestoContext fc) {
	    threadLocal.set(fc);
	    frestoContextExists = true;
	    LOGGER.fine("Set fresto context");
	}

	public static void unset() {
	    threadLocal.remove();
	    frestoContextExists = false;
	    LOGGER.fine("Unset fresto context");
	}

	public static FrestoContext get() {
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoCotnext does not exist");
		return null;
	    }
	    LOGGER.fine("Get fresto context");
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
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoCotnext does not exist");
		return;
	    }
	    // increase depth of context object
	    FrestoTracker.get().increaseDepth();
	}

	public static void endTrack() {
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoCotnext does not exist");
		return;
	    }
	    // decrease depth of context object

	    LOGGER.info("FrestoCotnext exists");
	    int depth = FrestoTracker.get().decreaseDepth();
	    if(depth == 0) {
		// Call close to close the ZMQ socket
		FrestoTracker.get().close();
		FrestoTracker.unset();
	    }
	}

	public static void captureHttpServletRequest(HttpServletRequest request, String typeName, String signatureName) {
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    HttpRequestEvent httpRequestEvent = new HttpRequestEvent(
		request.getMethod(),
		request.getLocalName(),
		request.getLocalPort(),
		request.getContextPath(),
		request.getServletPath(),
		//request.getHeader("fresto-uuid"),
		FrestoTracker.get().getUuid(),
		typeName,
		signatureName,
		System.currentTimeMillis()
		);
	    // Publish this event to monitoring server
	    LOGGER.info("HttpServletRequest event: sending");
	    //FrestoTracker.get().publishEventToMonitor("H", httpRequestEvent);
	    FrestoContextGlobal frestoContextGlobal = (FrestoContextGlobal) (request.getSession().getServletContext().getAttribute("frestoContextGlobal"));
	    frestoContextGlobal.publishEventToMonitor("H", httpRequestEvent);
	    LOGGER.info("HttpServletRequest event: sent");


	}

	public static void captureHttpServletResponse(HttpServletResponse response) {
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	}

}
    
