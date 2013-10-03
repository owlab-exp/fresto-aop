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

public aspect SpringDispatcherServletMonitor {
    private static Logger LOGGER = Logger.getLogger("SpringDispathcServletMonitor");

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
	    LOGGER.fine("FrestoContext created");

	    long startTime = System.currentTimeMillis();
	    FrestoTracker.captureHttpServletRequest(
	    	request, 
		thisJoinPoint.getSignature().getDeclaringTypeName(), 
		thisJoinPoint.getSignature().getName(),
		startTime
		);
	    FrestoTracker.get().setStartTime(startTime);

	    LOGGER.fine("HttpServletRequest captured");

	    FrestoTracker.beginTrack();

	    LOGGER.fine("Before proceed");
	    Object result = proceed(request, response);
	    LOGGER.fine("After proceed");
	    
	    FrestoTracker.captureHttpServletResponse(
	        request,
	    	response, 
		thisJoinPoint.getSignature().getDeclaringTypeName(), 
		thisJoinPoint.getSignature().getName(),
		System.currentTimeMillis()
		);
	    // Just after proceed
	    FrestoTracker.endTrack();


	    LOGGER.fine("Before return");
	    return result;
	}
}
