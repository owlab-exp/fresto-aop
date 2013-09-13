package fresto.aspects;

import java.util.UUID;
import java.util.logging.Logger;

public class FrestoContext {
    private static Logger LOGGER = Logger.getLogger("FrestoContext");

    public static int UUID_CREATOR_UI = 0;
    public static int UUID_CREATOR_AP = 1;

    private int uuidCreator = -1;
    private String uuid = null;
    private boolean isInitialized = false;

    private int depth = 0;

    private FrestoContext(String uuid, int uuidCreator) {
	LOGGER.info("uuid: " + uuid + ", uuidCreator: " + uuidCreator);
	this.uuid = uuid;
	this.uuidCreator = uuidCreator;
	this.isInitialized = true;
    }

    public static FrestoContext createInstance(String uuid) {
	return new FrestoContext(uuid, FrestoContext.UUID_CREATOR_UI);
    }

    public static FrestoContext createInstance() {
	return new FrestoContext(UUID.randomUUID().toString(), FrestoContext.UUID_CREATOR_AP);
    }
    
    public int getUUIDCreator() {
	if(!isInitialized) {
	    LOGGER.severe("FrestoContext is not initialized");
	    return -1;
	}
	return this.uuidCreator;
    }

    public String getUuid() {
	if(!isInitialized) {
	    LOGGER.severe("FrestoContext is not initialized");
	    return null;
	}
	return this.uuid;
    }

    public int increaseDepth() {
	if(!isInitialized) {
	    LOGGER.severe("FrestoContext is not initialized");
	    return -1;
	}
	depth++;
	LOGGER.info("[depth] " + depth);
	return depth;
    }

    public int decreaseDepth() {
	if(!isInitialized) {
	    LOGGER.severe("FrestoContext is not initialized");
	    return -1;
	}
	depth--;
	LOGGER.info("[depth] " + depth);
	return depth;
    }

    public int getDepth() {
	if(!isInitialized) {
	    LOGGER.severe("FrestoContext is not initialized");
	    return -1;
	}
	return this.depth;
    }
}
