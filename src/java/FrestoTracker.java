package fresto.aspects;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fresto.event.HttpRequestEvent;
import fresto.event.HttpResponseEvent;

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
	public static void createFrestoContext(FrestoContextGlobal frestoContextGlobal, String uuid) {
	    FrestoContext fc = FrestoContext.createInstance(frestoContextGlobal, uuid);
	    FrestoTracker.set(fc);
	}

	public static void createFrestoContext(FrestoContextGlobal frestoContextGlobal) {
	    FrestoContext fc = FrestoContext.createInstance(frestoContextGlobal);
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

	public static void captureHttpServletRequest(HttpServletRequest request, String typeName, String signatureName, long timestamp) {
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
			FrestoTracker.get().getDepth(),
			timestamp
		);
	    // Publish this event to monitoring server
	    LOGGER.fine("HttpServletRequest event: sending");
	    //FrestoTracker.get().publishEventToMonitor("H", httpRequestEvent);
	    //FrestoContextGlobal frestoContextGlobal = (FrestoContextGlobal) (request.getSession().getServletContext().getAttribute("frestoContextGlobal"));
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("HB", httpRequestEvent);
	    LOGGER.fine("HttpServletRequest event: sent");
	}

	public static void captureHttpServletResponse(HttpServletResponse response, String typeName, String signatureName, long timestamp) {
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    HttpResponseEvent httpResponseEvent = new HttpResponseEvent(
	    		response.getStatus(),
			FrestoTracker.get().getUuid(),
			typeName,
			signatureName,
			FrestoTracker.get().getDepth(),
			timestamp
		);

	    LOGGER.fine("HttpServletResponse event: sending");
	    //FrestoContextGlobal frestoContextGlobal = (FrestoContextGlobal) (request.getSession().getServletContext().getAttribute("frestoContextGlobal"));
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("HE", httpResponseEvent);
	    LOGGER.fine("HttpServletResponse event: sent");
	}

}
    
