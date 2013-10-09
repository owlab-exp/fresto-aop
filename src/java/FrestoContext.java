package fresto.aspects;

import java.util.UUID;
import java.util.logging.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.apache.thrift.TSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

public class FrestoContext {
    private static Logger LOGGER = Logger.getLogger("FrestoContext");

    public static int UUID_CREATOR_UI = 0;
    public static int UUID_CREATOR_AP = 1;

    private int uuidCreator = -1;
    private String uuid = null;
    private boolean isInitialized = false;

    private int depth = 0;
    private long startTime = -1L;

    private FrestoContextGlobal frestoContextGlobal;

    private FrestoContext(FrestoContextGlobal frestoContextGlobal, String uuid, int uuidCreator) {
	LOGGER.fine("uuid: " + uuid + ", uuidCreator: " + uuidCreator);
	this.uuid = uuid;

	this.uuidCreator = uuidCreator;
	this.frestoContextGlobal = frestoContextGlobal;

//	this.zmqContext = ZMQ.context(1);
//	this.publisher = this.zmqContext.socket(ZMQ.PUB);
//	//this.publisher.connect("tcp://" + pubHost + ":" + pubPort);
//	this.publisher.connect("tcp://fresto1.owlab.com:7002");
//	//this.publisher.send("H".getBytes(), ZMQ.SNDMORE);
//	//this.publisher.send("HI This is Seoul".getBytes(), 0);
//
//	LOGGER.info("JeroMQ publisher uses " + pubHost + ":" + pubPort );

	this.isInitialized = true;
    }

    public static FrestoContext createInstance(FrestoContextGlobal frestoContextGlobal, String uuid) {
	LOGGER.fine("uuid: " + uuid);
	return new FrestoContext(frestoContextGlobal, uuid, FrestoContext.UUID_CREATOR_UI);
    }

    public static FrestoContext createInstance(FrestoContextGlobal frestoContextGlobal) {
	LOGGER.fine("no uuid");
	return new FrestoContext(frestoContextGlobal, UUID.randomUUID().toString(), FrestoContext.UUID_CREATOR_AP);
    }

    public FrestoContextGlobal getFrestoContextGlobal() {
	return this.frestoContextGlobal;
    }

    public void close() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return;
	}
	//LOGGER.fine("Closing ZMQ socket");
	//this.publisher.close();
	//this.zmqContext.term();
    }
    
    public int getUUIDCreator() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return -1;
	}
	return this.uuidCreator;
    }

    public String getUuid() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return null;
	}
	return this.uuid;
    }

    public int increaseDepth() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return -1;
	}
	depth++;
	LOGGER.fine("[depth] " + depth);
	return depth;
    }

    public int decreaseDepth() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return -1;
	}
	depth--;
	LOGGER.fine("[depth] " + depth);
	return depth;
    }

    public int getDepth() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return -1;
	}
	return this.depth;
    }

    public void setStartTime(long timestamp) {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return;
	}
	this.startTime = timestamp;
    }

    public long getStartTime() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return -1L;
	}
	return this.startTime;
    }
}
