/**************************************************************************************
 * Copyright 2013 TheSystemIdeas, Inc and Contributors. All rights reserved.          *
 *                                                                                    *
 *     https://github.com/owlab/fresto                                                *
 *                                                                                    *
 *                                                                                    *
 * ---------------------------------------------------------------------------------- *
 * This file is licensed under the Apache License, Version 2.0 (the "License");       *
 * you may not use this file except in compliance with the License.                   *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 * 
 **************************************************************************************/
package fresto.aspects;

import java.util.logging.Logger;
import java.util.Properties;
import java.io.IOException;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;


import org.apache.thrift.TSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

public class FrestoContextGlobal {
    private static Logger _logger = Logger.getLogger("FrestoContextGlobal");

    private String pubHost = null;
    private String pubPort = null;
    private ZMQ.Context zmqContext;
    private ZMQ.Socket publisher;
    private TSerializer serializer;

    public FrestoContextGlobal() {
		_logger.info("Initializing...");
		Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("fresto.properties"));
		} catch(IOException ioe) {
			_logger.severe("Error loading fresto.properties: " + ioe.getMessage());
		}
		String eventHubHostName = properties.getProperty("eventhub.hostname");
		String eventHubPort = properties.getProperty("eventhub.port");
		if(eventHubHostName == null || eventHubPort == null) {
			_logger.severe("No event hub host/port defined. Dummy local host/port will be used");
			pubHost = "localhost";
			pubPort = "7000";
		} else {
			pubHost = eventHubHostName;
			pubPort = eventHubPort;
		}

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
