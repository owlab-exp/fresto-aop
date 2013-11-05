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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
//import java.sql.ResultSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.util.logging.Logger;
import edu.emory.mathcs.util.collections.WeakIdentityHashMap;

public aspect SQLExecutionAspect {
    private static Logger LOGGER = Logger.getLogger("SQLExecutionAspect"); 

    public SQLExecutionAspect() {
		LOGGER.fine("Created");
    }

	/**
	 * To keep SQL text while PreparedStatement being used
	 */
	//////////// Pointcuts /////////////////////
    public pointcut jdbcCreatePreparedStatement(String sqlText) :
        call(Statement+ Connection+.*(String, ..)) && within(Connection+) && args(sqlText, ..);

	public pointcut jdbcCreatePreparedStatementTop(String sqlText) :
		jdbcCreatePreparedStatement(sqlText) && !cflowbelow(jdbcCreatePreparedStatement(*));
	
	/////////// Do something in the pointcuts //////
    Object around(final String sqlText) : jdbcCreatePreparedStatementTop(sqlText) {
       	LOGGER.fine("[thisJoinPoint.getSignature.name] " + thisJoinPoint.getSignature().getName());
		LOGGER.fine("[this.EnclosingJoinPoint.staticPart] " + thisEnclosingJoinPointStaticPart.getSignature());
       	LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
       	LOGGER.info("[SQL] " + sqlText);

       	Statement statement = (Statement)proceed(sqlText);

		LOGGER.info("Putting statement and sqlText");

		//statementSqlMap.put(statement, sqlText);
		keepSql(statement, sqlText);

       	return statement;
    }

	/**
	 * To probe call of prepared statements;
	 */
	//////////// Pointcuts /////////////////////
	public pointcut jdbcStatementExecute() :
		call(* java.sql..*.execute*(..));

	public pointcut jdbcStatementExecuteWithTarget(Statement statement) :
		call(* java.sql..*.execute*(..)) && target(statement) && if(aspectOf().getSql(statement) != null);
	// 
	//public pointcut jdbcStatementExecuteTop(Statement statement) :
	//	jdbcStatementExecuteWithTarget(statement) && !cflowbelow(jdbcStatementExecute());
	
	/////////// Do something in the pointcuts //////
	Object around(final Statement statement) : jdbcStatementExecuteWithTarget(statement) { 
       	LOGGER.fine("[thisJoinPoint.getSignature.name] " + thisJoinPoint.getSignature().getName());
		LOGGER.fine("[this.JoinPoint.getSignature] " + thisJoinPoint.getSignature());

		long startTime = System.currentTimeMillis();

		LOGGER.info("Getting statement and sqlText");
		//String sqlText = (String) statementSqlMap.get(statement);
		String sqlText = getSql(statement);

        FrestoStopWatch stopWatch = new FrestoStopWatch();
		if(sqlText != null) {
			sqlText = sqlText.trim();
        	FrestoTracker.beginTrack();
        	FrestoTracker.setDepth(4);
        	FrestoTracker.captureSqlCall(
        	    null,
        	    sqlText,
        	    stopWatch.getStartTime()
        	);
		}

		Object object = proceed(statement);
		
		stopWatch.stop();
		if(sqlText != null) {
        	FrestoTracker.setDepth(4);
        	FrestoTracker.captureSqlReturn(
        	    null,
        	    sqlText,
        	    stopWatch.getElapsedTime(),
        	    stopWatch.getEndTime()
        	);
        	FrestoTracker.endTrack();
		}

		return object;
	}

	/**
	 * To probe call of dynamic statements;
	 */
	//////////// Pointcuts /////////////////////
	public pointcut jdbcExecuteQuery(String sqlText) :
		call(* Statement.execute*(String, ..)) && within(Statement+) && args(sqlText, ..);
	
	public pointcut jdbcExecuteQueryTop(String sqlText) :
		jdbcExecuteQuery(sqlText) && !cflowbelow(jdbcExecuteQuery(*));
	
	/////////// Do something in the pointcuts //////
	Object around(final String sqlText) : jdbcExecuteQueryTop(sqlText) {
		LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

		LOGGER.info("[SQL] " + sqlText);

        FrestoStopWatch stopWatch = new FrestoStopWatch();
		if(sqlText == null) {
        	FrestoTracker.beginTrack();
        	FrestoTracker.setDepth(4);
        	FrestoTracker.captureSqlCall(
        	    null,
        	    sqlText,
        	    stopWatch.getStartTime()
        	);
		}

		Object object = proceed(sqlText);
		
		stopWatch.stop();
		if(sqlText == null) {
        	FrestoTracker.setDepth(4);
        	FrestoTracker.captureSqlReturn(
        	    null,
        	    sqlText,
        	    stopWatch.getElapsedTime(),
        	    stopWatch.getEndTime()
        	);
        	FrestoTracker.endTrack();
		}

		return object;
	}

	/**
	 * To save batch statements;
	 */
	//////////// Pointcuts /////////////////////
	public pointcut jdbcAddBatch(Statement statement, String sqlText) :
		call(* Statement+.addBatch(String)) && within(Statement+) && this(statement) && args(sqlText);
	
	public pointcut jdbcAddBatchTop(Statement statement, String sqlText) :
		jdbcAddBatch(statement, sqlText) && !cflowbelow(jdbcAddBatch(*, *));
	
	/////////// Do something in the pointcuts //////
	Object around(final Statement statement, final String sqlText) : jdbcAddBatchTop(statement, sqlText) {
		LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

		LOGGER.info("[SQL] " + sqlText);

		addSqlToList(statement, sqlText);

		Object object = proceed(statement, sqlText);

		return object;
	}

	/**
	 * To probe batch call;
	 */
	//////////// Pointcuts /////////////////////
	public pointcut jdbcExecuteBatch(Statement statement) :
		call(* Statement+.executeBatch(..)) && within(Statement+) && this(statement) && if(aspectOf().getSqlList(statement) != null);
	public pointcut jdbcExecuteBatchTop(Statement statement) :
		jdbcExecuteBatch(statement) && !cflowbelow(jdbcExecuteBatch(*));
	
	/////////// Do something in the pointcuts //////
	Object around(final Statement statement) : jdbcExecuteBatchTop(statement) {
		LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

		//List<String> sqlTextList = (List<String>) statementSqlMap.get(statement);
		List<String> sqlTextList = getSqlList(statement);

        FrestoStopWatch stopWatch = new FrestoStopWatch();
        FrestoTracker.beginTrack();
        FrestoTracker.setDepth(4);
        FrestoTracker.captureSqlCall(
            null,
            toText(sqlTextList),
            stopWatch.getStartTime()
        );

		Object object = proceed(statement);

		stopWatch.stop();
       	FrestoTracker.setDepth(4);
       	FrestoTracker.captureSqlReturn(
       	    null,
            toText(sqlTextList),
       	    stopWatch.getElapsedTime(),
       	    stopWatch.getEndTime()
       	);
       	FrestoTracker.endTrack();

		//
		return object;
	}

	private String toText(List<String> stringList) {
		StringBuilder sb = new StringBuilder();
		for(String s: stringList) {
			sb.append(s);
			sb.append(";");
		}
		return sb.toString();
	}

    private Map statementSqlMap = new WeakIdentityHashMap();

	private synchronized void keepSql(Statement statement, String sqlText) {
		this.statementSqlMap.put(statement, sqlText);
	}

	private synchronized String getSql(Statement statement) {
		return (String) this.statementSqlMap.get(statement);
	}

	private synchronized void addSqlToList(Statement statement, String sqlText) {
		if(statementSqlMap.get(statement) == null) {
			List<String> sqlTextList = new ArrayList<String>();
			sqlTextList.add(sqlText);
			statementSqlMap.put(statement, sqlTextList);
		} else {
			List<String> sqlTextList = (List<String>) statementSqlMap.get(statement);
			sqlTextList.add(sqlText);
		}
	}

	private synchronized List getSqlList(Statement statement) {
		return (List<String>) statementSqlMap.get(statement);
	}

}
