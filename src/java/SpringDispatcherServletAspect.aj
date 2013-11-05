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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.DispatcherServlet;
//import org.springframework.web.context.ContextLoaderServlet;
//import org.springframework.web.context.ContextLoaderListener;

public aspect SpringDispatcherServletAspect {
    private static Logger LOGGER = Logger.getLogger("SpringDispatcherServletAspect");

    /** In case of Spring Framework application, all servlet request handled by DispatcherServlet */
    public pointcut dispatcherServletService(HttpServletRequest request, HttpServletResponse response) :
    	execution(void DispatcherServlet.doService(HttpServletRequest, HttpServletResponse)) && args (request, response);

    Object around(HttpServletRequest request, HttpServletResponse response):
    	dispatcherServletService(request, response) {
	    
	    //System.out.println("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    LOGGER.fine("[thisJoinPoint.getSignature.declaringTypeName] " + thisJoinPoint.getSignature().getDeclaringTypeName());
	    LOGGER.fine("[thisJoinPoint.getSignature.name] " + thisJoinPoint.getSignature().getName());

	    String frestoUuid = request.getHeader("fresto-uuid");
	    FrestoContextGlobal frestoContextGlobal = (FrestoContextGlobal) (request.getSession().getServletContext().getAttribute("frestoContextGlobal"));
	    if(frestoUuid != null)
	    	FrestoTracker.createFrestoContext(frestoContextGlobal, request.getHeader("fresto-uuid"));
	    else 
			FrestoTracker.createFrestoContext(frestoContextGlobal);
	    LOGGER.info("FrestoContext created");

		FrestoStopWatch stopWatch = new FrestoStopWatch();
	    FrestoTracker.beginTrack();
		FrestoTracker.setDepth(1);

	    FrestoTracker.captureHttpServletRequest( 
			request, 
			thisJoinPoint.getSignature().getDeclaringTypeName(), 
			thisJoinPoint.getSignature().getName(),
			stopWatch.getStartTime()
		);
	    //FrestoTracker.get().setStartTime(startTime);

	    LOGGER.fine("HttpServletRequest captured");


	    LOGGER.fine("Before proceed");
	    Object result = null;
		try {
	    	result = proceed(request, response);

		} finally {
	    
			stopWatch.stop();
			FrestoTracker.setDepth(1);
	    	FrestoTracker.captureHttpServletResponse(
	    	    request,
	    		response, 
				thisJoinPoint.getSignature().getDeclaringTypeName(), 
				thisJoinPoint.getSignature().getName(),
				stopWatch.getElapsedTime(),
				stopWatch.getEndTime()
			);

	    	LOGGER.info("endTrack to determine releasing FrestoContext");
	    	FrestoTracker.endTrack();
		}


	    //LOGGER.fine("Before return");
	    return result;
	}
}
