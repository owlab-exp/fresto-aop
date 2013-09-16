package fresto.aspects;

import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.DispatcherServlet;

public aspect DispatcherServletMonitor {
    private static Logger _logger = Logger.getLogger("DispathcServletMonitor");

    /** In case of Spring Framework application, all servlet request handled by DispatcherServlet */
    public pointcut dispatcherServletService(HttpServletRequest request, HttpServletResponse response) :
    	execution(void DispatcherServlet.doService(HttpServletRequest, HttpServletResponse)) && args (request, response);

    Object around(HttpServletRequest request, HttpServletResponse response):
    	dispatcherServletService(request, response) {
	    
	    //System.out.println("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    _logger.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    String frestoUuid = request.getHeader("fresto-uuid");
	    if(frestoUuid != null)
	    	FrestoTracker.createFrestoContext(request.getHeader("fresto-uuid"));
	    else 
		FrestoTracker.createFrestoContext();
	    _logger.info("FrestoContext created");

	    FrestoTracker.captureHttpServletRequest(request);
	    _logger.info("HttpServletRequest captured");

	    FrestoTracker.beginTrack();

	    Object result = proceed(request, response);

	    // Just after proceed
	    FrestoTracker.endTrack();


	    return result;
	}
}
