package fresto.aspects;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fresto.data.ResourceID;
import fresto.data.OperationID;
import fresto.data.EntryOperationCallEdge;
import fresto.data.EntryOperationReturnEdge;
import fresto.data.DataUnit;
import fresto.data.Pedigree;
import fresto.data.FrestoData;

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

	    LOGGER.fine("FrestoCotnext exists");
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

	    OperationID operationId = new OperationID();
	    operationId.setTypeName(typeName);
	    operationId.setOperationName(signatureName);

	    ResourceID resourceId = ResourceID.url(request.getRequestURL().toString());

	    EntryOperationCallEdge entryOperationCallEdge = new EntryOperationCallEdge();
	    entryOperationCallEdge.setResourceId(resourceId);
	    entryOperationCallEdge.setOperationId(operationId);
	    entryOperationCallEdge.setLocalHost(request.getLocalName());
	    entryOperationCallEdge.setLocalPort(request.getLocalPort());
	    entryOperationCallEdge.setContextPath(request.getContextPath());
	    entryOperationCallEdge.setServletPath(request.getServletPath());
	    entryOperationCallEdge.setHttpMethod(request.getMethod());
	    entryOperationCallEdge.setTimestamp(timestamp);
	    entryOperationCallEdge.setUuid(FrestoTracker.get().getUuid());

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(System.currentTimeMillis());

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.entryOperationCallEdge(entryOperationCallEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("EntryOperationCallEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("EB", frestoData);
	    LOGGER.fine("EntryOperationCallEdge event: sent");
	}

	public static void captureHttpServletResponse(HttpServletRequest request, HttpServletResponse response, String typeName, String signatureName, long timestamp) {
	    if(!frestoContextExists) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    OperationID operationId = new OperationID();
	    operationId.setTypeName(typeName);
	    operationId.setOperationName(signatureName);

	    ResourceID resourceId = ResourceID.url(request.getRequestURL().toString());

	    EntryOperationReturnEdge entryOperationReturnEdge = new EntryOperationReturnEdge();
	    entryOperationReturnEdge.setOperationId(operationId);
	    entryOperationReturnEdge.setResourceId(resourceId);
	    entryOperationReturnEdge.setHttpStatus(response.getStatus());
	    int elapsedTime = (int)(timestamp - FrestoTracker.get().getStartTime());
	    entryOperationReturnEdge.setElapsedTime(elapsedTime);
	    entryOperationReturnEdge.setTimestamp(timestamp);
	    entryOperationReturnEdge.setUuid(FrestoTracker.get().getUuid());

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(System.currentTimeMillis());

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.entryOperationReturnEdge(entryOperationReturnEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("EntryOperationReturnEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("EF", frestoData);
	    LOGGER.fine("EntryOperationReturnEdge event: sent");
	}

}
    
