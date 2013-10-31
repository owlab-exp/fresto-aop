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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fresto.data.ResourceID;
import fresto.data.OperationID;
import fresto.data.SqlID;
import fresto.data.EntryOperationCallEdge;
import fresto.data.EntryOperationReturnEdge;
import fresto.data.OperationCallEdge;
import fresto.data.OperationReturnEdge;
import fresto.data.SqlCallEdge;
import fresto.data.SqlReturnEdge;
import fresto.data.DataUnit;
import fresto.data.Pedigree;
import fresto.data.FrestoData;

public class FrestoTracker {
	private static Logger LOGGER = Logger.getLogger("FrestoTracker");

	private static final ThreadLocal<FrestoContext> threadLocal = new ThreadLocal<FrestoContext>();

	//private static boolean frestoContextExists = false;


	public static void set(FrestoContext fc) {
	    threadLocal.set(fc);
	    //frestoContextExists = true;
	    LOGGER.fine("Set FrestoContext");
	}

	public static void unset() {
	    threadLocal.remove();
	    //frestoContextExists = false;
	    LOGGER.info("FrestoContext release object in ThreadLocal");
	}

	public static FrestoContext get() {
	    LOGGER.fine("Get fresto context");
	    return threadLocal.get();
	}

	public static boolean frestoContextExists() {
		if(FrestoTracker.get() == null)
			return false;
		else 
			return true;
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

	public static void setDepth(int depth) {
	    if(!frestoContextExists()) {
		LOGGER.warning("FrestoCotnext does not exist");
		return;
	    }
	    // 
	    FrestoTracker.get().setDepth(depth);
	}

	// Per operation call
	public static void beginTrack() {
	    if(!frestoContextExists()) {
		LOGGER.warning("FrestoCotnext does not exist");
		return;
	    }
	    // increase Sequence of context object
	    FrestoTracker.get().increaseSequence();
	}

	// Per operation return
	public static void endTrack() {
	    LOGGER.info("FrestoContext.endTrack");
	    if(!frestoContextExists()) {
			LOGGER.warning("FrestoContext does not exist");
			return;
	    }
	    // decrease depth of context object

	    //int depth = FrestoTracker.get().decreaseDepth();
	    LOGGER.info("FrestoContext.depth = " + FrestoTracker.get().getDepth());
	    if(FrestoTracker.get().getDepth() == 1) { // Means this is really end of a call flow.
	    	LOGGER.info("FrestoContext closing...");
			FrestoTracker.get().close();
			FrestoTracker.unset();
	    }
	}

	public static void captureHttpServletRequest(HttpServletRequest request, String typeName, String signatureName, long timestamp) {
	    if(!frestoContextExists()) {
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
	    entryOperationCallEdge.setSequence(FrestoTracker.get().getSequence());
	    entryOperationCallEdge.setDepth(FrestoTracker.get().getDepth());

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(System.currentTimeMillis());

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.entryOperationCallEdge(entryOperationCallEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("EntryOperationCallEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
		//DEBUG
		LOGGER.info("FrestoContextGlobal=" + frestoContextGlobal);

	    frestoContextGlobal.publishEventToMonitor("EB", frestoData);
	    LOGGER.fine("EntryOperationCallEdge event: sent");
	}

	public static void captureHttpServletResponse(HttpServletRequest request, HttpServletResponse response, String typeName, String signatureName, int elapsedTime, long timestamp) {
	    if(!frestoContextExists()) {
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
	    entryOperationReturnEdge.setServletPath(request.getServletPath());
	    entryOperationReturnEdge.setHttpStatus(response.getStatus());
	    entryOperationReturnEdge.setElapsedTime(elapsedTime);
	    entryOperationReturnEdge.setTimestamp(timestamp);
	    entryOperationReturnEdge.setUuid(FrestoTracker.get().getUuid());
	    entryOperationReturnEdge.setSequence(FrestoTracker.get().getSequence());
	    entryOperationReturnEdge.setDepth(FrestoTracker.get().getDepth());

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

	public static void captureOperationCall(String typeName, String operationName, long timestamp) {
	    if(!frestoContextExists()) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    OperationID operationId = new OperationID();
	    operationId.setTypeName(typeName);
	    operationId.setOperationName(operationName);

	    OperationCallEdge operationCallEdge = new OperationCallEdge();
	    operationCallEdge.setOperationId(operationId);
	    operationCallEdge.setTimestamp(timestamp);
	    operationCallEdge.setUuid(FrestoTracker.get().getUuid());
	    operationCallEdge.setSequence(FrestoTracker.get().getSequence());
	    operationCallEdge.setDepth(FrestoTracker.get().getDepth());

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(timestamp);

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.operationCallEdge(operationCallEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("OperationCallEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("OB", frestoData);
	    LOGGER.fine("OperationCallEdge event: sent");
	}

	public static void captureOperationReturn(String typeName, String operationName, int elapsedTime, long timestamp) {
	    if(!frestoContextExists()) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    OperationID operationId = new OperationID();
	    operationId.setTypeName(typeName);
	    operationId.setOperationName(operationName);

	    OperationReturnEdge operationReturnEdge = new OperationReturnEdge();
	    operationReturnEdge.setOperationId(operationId);
	    operationReturnEdge.setElapsedTime(elapsedTime);
	    operationReturnEdge.setTimestamp(timestamp);
	    operationReturnEdge.setUuid(FrestoTracker.get().getUuid());
	    operationReturnEdge.setSequence(FrestoTracker.get().getSequence());
	    operationReturnEdge.setDepth(FrestoTracker.get().getDepth());

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(timestamp);

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.operationReturnEdge(operationReturnEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("OperationReturnEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("OF", frestoData);
	    LOGGER.fine("OperationReturnEdge event: sent");
	}

	public static void captureSqlCall(String dbUrl, String sqlText, long timestamp) {
	    if(!frestoContextExists()) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    SqlID sqlId = new SqlID();
	    sqlId.setDatabaseUrl(dbUrl);
	    sqlId.setSql(sqlText);

	    SqlCallEdge sqlCallEdge = new SqlCallEdge();
	    sqlCallEdge.setSqlId(sqlId);
	    sqlCallEdge.setTimestamp(timestamp);
	    sqlCallEdge.setUuid(FrestoTracker.get().getUuid());
	    sqlCallEdge.setSequence(FrestoTracker.get().getSequence());
	    sqlCallEdge.setDepth(FrestoTracker.get().getDepth());

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(timestamp);

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.sqlCallEdge(sqlCallEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("SqlCallEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("SB", frestoData);
	    LOGGER.fine("SqlCallEdge event: sent");
	}

	public static void captureSqlReturn(String dbUrl, String sqlText, int elapsedTime, long timestamp) {
	    if(!frestoContextExists()) {
		LOGGER.warning("FrestoContext does not exists");
		return;
	    }

	    SqlID sqlId = new SqlID();
	    sqlId.setDatabaseUrl(dbUrl);
	    sqlId.setSql(sqlText);

	    SqlReturnEdge sqlReturnEdge = new SqlReturnEdge();
	    sqlReturnEdge.setSqlId(sqlId);
	    sqlReturnEdge.setElapsedTime(elapsedTime);
	    sqlReturnEdge.setUuid(FrestoTracker.get().getUuid());
	    sqlReturnEdge.setSequence(FrestoTracker.get().getSequence());
	    sqlReturnEdge.setDepth(FrestoTracker.get().getDepth());
	    sqlReturnEdge.setTimestamp(timestamp);

	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(timestamp);

	    FrestoData frestoData = new FrestoData();
	    frestoData.setDataUnit(DataUnit.sqlReturnEdge(sqlReturnEdge));
	    frestoData.setPedigree(pedigree);
	    // Publish this event to monitoring server
	    LOGGER.fine("SqlReturnEdge event: sending");
	    FrestoContextGlobal frestoContextGlobal = FrestoTracker.get().getFrestoContextGlobal();
	    frestoContextGlobal.publishEventToMonitor("SF", frestoData);
	    LOGGER.fine("SqlReturnEdge event: sent");
	}

}
    
