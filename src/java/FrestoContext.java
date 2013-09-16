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

    private static String pubHost = "fresto1.owlab.com";
    //private static String pubHost = "*";
    private static int pubPort = 7002;
    private ZMQ.Context zmqContext; //= ZMQ.context(1);
    private ZMQ.Socket publisher;

    private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());

    private FrestoContext(String uuid, int uuidCreator) {
	LOGGER.fine("uuid: " + uuid + ", uuidCreator: " + uuidCreator);
	this.uuid = uuid;

	this.uuidCreator = uuidCreator;

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

    public static FrestoContext createInstance(String uuid) {
	LOGGER.fine("uuid: " + uuid);
	return new FrestoContext(uuid, FrestoContext.UUID_CREATOR_UI);
    }

    public static FrestoContext createInstance() {
	LOGGER.fine("no uuid");
	return new FrestoContext(UUID.randomUUID().toString(), FrestoContext.UUID_CREATOR_AP);
    }

    public void close() {
	if(!isInitialized) {
	    LOGGER.warning("FrestoContext is not initialized");
	    return;
	}
	LOGGER.fine("Closing ZMQ socket");
	this.publisher.close();
	this.zmqContext.term();
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

    //public void publishEventToMonitor(String envelope, TBase base) {
    //    if(!isInitialized) {
    //        LOGGER.warning("FrestoContext is not initialized");
    //        return;
    //    }

    //    byte[] eventBytes = null;
    //    try {
    //        eventBytes = serializer.serialize(base);
    //    } catch(TException te) {
    //        LOGGER.warning("TSerializer exception: " + te.getMessage());
    //        return;
    //    }
    //    
    //    //byte[] serializedEvent = serializer.serialize(httpServletRequestEvent);
    //    LOGGER.info("[eventBytes ] " + eventBytes.length + " bytes");
    //    this.publisher.send(envelope.getBytes(), ZMQ.SNDMORE);
    //    this.publisher.send(eventBytes, 0);

    //    //LOGGER.info("[eventBytes ] sent " + i + " count");
    //    LOGGER.info("[eventBytes ] sent ");
    //    
    //}
}
