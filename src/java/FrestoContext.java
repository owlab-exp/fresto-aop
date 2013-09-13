package fresto.aspects;

import java.util.UUID;

public class FrestoContext {

    public static int UUID_CREATOR_UI = 0;
    public static int UUID_CREATOR_AP = 1;

    private int uuidCreator = -1;
    private String uuid = null;
    private boolean isInitialized = false;

    private int depth = 0;

    private FrestoContext(String uuid, int uuidCreator) {
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
	return this.uuidCreator;
    }

    public String getUuid() {
	return this.uuid;
    }

    public void increaseDepth() {
	depth++;
    }

    public void decreaseDepth() {
	depth--;
    }

    public int getDepth() {
	return this.depth;
    }
}
