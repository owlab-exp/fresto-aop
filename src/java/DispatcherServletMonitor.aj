package fresto.aspects;

import java.util.logging.Logger;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.DispatcherServlet;
//import org.springframework.web.context.ContextLoaderServlet;
import org.springframework.web.context.ContextLoaderListener;

public aspect DispatcherServletMonitor {
    private static Logger _logger = Logger.getLogger("DispathcServletMonitor");

    /** To weave in ContextLoaderServlet Init */
    /** Deprecated for since Servlet API 2.4 */
//    public pointcut contextLoaderServletInit() :
//    	execution(void ContextLoaderServlet.init());
//
//    Object around():
//    	contextLoaderServletInit() {
//	    _logger.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
//	    Object result = proceed();
//	    return result;
//	}
    /** To weave in servlet context initialization time */
    public pointcut contextLoaderListenerInitialized(ServletContextEvent event) :
    	execution(void ContextLoaderListener.contextInitialized(ServletContextEvent)) && args(event);
    
    Object around(ServletContextEvent event) :
    	contextLoaderListenerInitialized(event) {
	    _logger.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    // To store values to servlet context
	    // Mainly for ZMQ now
	    ServletContext servletContext = event.getServletContext();
	    servletContext.setAttribute("frestoContextGlobal", new FrestoContextGlobal());

	    Object result = proceed(event);
	    return result;
	}

    /** To weave in servlet context destory time */
    public pointcut contextLoaderListenerDestroyed(ServletContextEvent event) :
    	execution(void ContextLoaderListener.contextDestroyed(ServletContextEvent)) && args(event);
    
    Object around(ServletContextEvent event) :
    	contextLoaderListenerDestroyed(event) {
	    _logger.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    // To store values to servlet context
	    // Mainly for ZMQ closing now
	    // It will be better to place after proceed method
	    ServletContext servletContext = event.getServletContext();
	    FrestoContextGlobal frestoContextGlobal = (FrestoContextGlobal) servletContext.getAttribute("frestoContextGlobal");
	    frestoContextGlobal.close();
	    servletContext.removeAttribute("frestoContextGlobal");

	    Object result = proceed(event);
	    return result;
	}

    /** To weave in each Servlet Init */
    public pointcut genericServletInit() :
    	execution(void GenericServlet+.init()) && !within(GenericServlet);

    Object around():
    	genericServletInit() {
	    _logger.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    Object result = proceed();
	    return result;
	}

    /** To weave in each Servlet Destroy */
    public pointcut genericServletDestory() :
    	execution(void GenericServlet+.destroy());// && !within(GenericServlet);

    Object around():
    	genericServletDestory() {
	    _logger.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    Object result = proceed();
	    return result;
	}

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