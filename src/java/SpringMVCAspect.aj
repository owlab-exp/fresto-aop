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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.FrameworkServlet;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.util.logging.Logger;

public aspect SpringMVCAspect {
    private static Logger LOGGER = Logger.getLogger("SpringMVCAspect");

    public pointcut springController(HttpServletRequest request, HttpServletResponse response) :
		execution(ModelAndView Controller+.*(HttpServletRequest, HttpServletResponse)) && !within(AbstractController) && args (request, response);

    public pointcut springDao() :
		execution(* org.springframework.samples.jpetstore.dao.*Dao.*(..));

    Object around(HttpServletRequest request, HttpServletResponse response):
    	springController(request, response) {
	    LOGGER.info("[SpringController thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

		String typeName = thisJoinPoint.getSignature().getDeclaringTypeName();
		String operationName = thisJoinPoint.getSignature().getName();
		FrestoStopWatch stopWatch = new FrestoStopWatch();
	    FrestoTracker.beginTrack();
		FrestoTracker.setDepth(2);
		FrestoTracker.captureOperationCall(
			typeName,
			operationName,
			stopWatch.getStartTime()
		);

	    Object result = proceed(request, response);

		stopWatch.stop();
		FrestoTracker.setDepth(2);
		FrestoTracker.captureOperationReturn(
			typeName,
			operationName,
			stopWatch.getElapsedTime(),
			stopWatch.getEndTime()
		);
	    FrestoTracker.endTrack();

	    return result;
	}
	
    Object around():
    	springDao() {
	    LOGGER.info("[SpringDao thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

		String typeName = thisJoinPoint.getSignature().getDeclaringTypeName();
		String operationName = thisJoinPoint.getSignature().getName();
		FrestoStopWatch stopWatch = new FrestoStopWatch();
	    FrestoTracker.beginTrack();
		FrestoTracker.setDepth(3);
		FrestoTracker.captureOperationCall(
			typeName,
			operationName,
			stopWatch.getStartTime()
		);

	    Object result = proceed();

		stopWatch.stop();
		FrestoTracker.setDepth(3);
		FrestoTracker.captureOperationReturn(
			typeName,
			operationName,
			stopWatch.getElapsedTime(),
			stopWatch.getEndTime()
		);
	    FrestoTracker.endTrack();

	    return result;
	}
	
}
