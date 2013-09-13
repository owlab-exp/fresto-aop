package fresto.aspects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.DispatcherServlet;

public aspect DispatcherServletMonitor {

    /** In case of Spring Framework application, all servlet request handled by DispatcherServlet */
    public pointcut dispatcherServletService(HttpServletRequest request, HttpServletResponse response) :
    	execution(void DispatcherServlet.doService(HttpServletRequest, HttpServletResponse)) && args (request, response);

    Object around(HttpServletRequest request, HttpServletResponse response):
    	dispatcherServletService(request, response) {
	    System.out.println("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    FrestoTracker.createFrestoContext(request.getHeader("fresto-uuid"));
	    FrestoTracker.beginTrack();

	    Object result = proceed(request, response);

	    // Just after proceed
	    FrestoTracker.endTrack();

//	    workOnResponse(response);

	    return result;
	}
}
