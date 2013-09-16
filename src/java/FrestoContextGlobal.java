package fresto.aspects;

import java.util.logging.Logger;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;


import org.apache.thrift.TSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

public class FrestoContextGlobal {
    private static Logger _logger = Logger.getLogger("FrestoContextGlobal");

    private String pubHost = "fresto1.owlab.com";
    private int pubPort = 7002;
    private ZMQ.Context zmqContext;
    private ZMQ.Socket publisher;
    private TSerializer serializer;

    public FrestoContextGlobal() {
	_logger.info("Initializing...");

	zmqContext = ZMQ.context(1);
	publisher = zmqContext.socket(ZMQ.PUB);
	publisher.connect("tcp://" + pubHost + ":" + pubPort);

	serializer = new TSerializer(new TBinaryProtocol.Factory());


	_logger.info("Started with JeroMQ(" + pubHost + ":" + pubPort + ")");
    }

    public void close() {
	_logger.info("Closing...");
	publisher.close();
	zmqContext.term();
    }

    public synchronized void publishEventToMonitor(String topic, TBase base) {
	byte[] eventBytes = null;
	try {
	    eventBytes = serializer.serialize(base);
	} catch(TException te) {
	    _logger.warning("Exceptio occurred: " + te.getMessage());
	    return;
	}

	publisher.send(topic.getBytes(), ZMQ.SNDMORE);
	publisher.send(eventBytes, 0);
    }
}