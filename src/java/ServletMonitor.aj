package fresto.aspects;

import java.util.logging.Logger;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

//import org.springframework.web.servlet.DispatcherServlet;
//import org.springframework.web.context.ContextLoaderServlet;
//import org.springframework.web.context.ContextLoaderListener;

public aspect ServletMonitor {
    private static Logger LOGGER = Logger.getLogger("ServletMonitor");

    /** To weave in ContextLoaderServlet Init */
    /** Deprecated for since Servlet API 2.4 */
//    public pointcut contextLoaderServletInit() :
//    	execution(void ContextLoaderServlet.init());
//
//    Object around():
//    	contextLoaderServletInit() {
//	    LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
//	    Object result = proceed();
//	    return result;
//	}

    /** To weave in servlet context initialization time */
    public pointcut servletContextListenerInitialized(ServletContextEvent event) :
    	execution(void ServletContextListener+.contextInitialized(ServletContextEvent)) && args(event);
    
    Object around(ServletContextEvent event) :
    	servletContextListenerInitialized(event) {
	    LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    // To store values to servlet context
	    // Mainly for ZMQ now
	    ServletContext servletContext = event.getServletContext();
	    // To avoid multiput setting
	    Object o = servletContext.getAttribute("frestoContextGlobal") ;
	    if(o == null) {
	    	LOGGER.info("[frestoContextGlobal] not exist in servlet context");
	    	servletContext.setAttribute("frestoContextGlobal", new FrestoContextGlobal());
	    } else {
	    	LOGGER.info("[frestoContextGlobal] already exist in servlet context");
	    }

	    Object result = proceed(event);
	    return result;
	}

    /** To weave in servlet context destory time */
    public pointcut servletContextListenerDestroyed(ServletContextEvent event) :
    	execution(void ServletContextListener+.contextDestroyed(ServletContextEvent)) && args(event);
    
    Object around(ServletContextEvent event) :
    	servletContextListenerDestroyed(event) {
	    LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    // To store values to servlet context
	    // Mainly for ZMQ closing now
	    // It will be better to place after proceed method
	    ServletContext servletContext = event.getServletContext();
	    FrestoContextGlobal frestoContextGlobal = (FrestoContextGlobal) servletContext.getAttribute("frestoContextGlobal");
	    if(frestoContextGlobal != null) {
	    	frestoContextGlobal.close();
	    	servletContext.removeAttribute("frestoContextGlobal");
	    }

	    Object result = proceed(event);
	    return result;
	}

    /**
     * To weave in ServletRequestListener
     * however, this is not the case of Spring Framework
     */
    public pointcut servletRequestListenerInitialized(ServletRequestEvent event) :
	execution(void ServletRequestListener+.requestInitialized(ServletRequestEvent)) && args(event);

    Object around(ServletRequestEvent event) :
    	servletRequestListenerInitialized(event) {
	    LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    LOGGER.info("[] not weaved.");

	    Object result = proceed(event);
	    return result;
	}

    public pointcut servletRequestListenerDestroyed(ServletRequestEvent event) :
	execution(void ServletRequestListener+.requestDestroyed(ServletRequestEvent)) && args(event);

    Object around(ServletRequestEvent event) :
    	servletRequestListenerDestroyed(event) {
	    LOGGER.info("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    LOGGER.info("[] not weaved.");

	    Object result = proceed(event);
	    return result;
	}
    /** To weave in each Servlet Init */
    public pointcut genericServletInit() :
    	execution(void GenericServlet+.init()) && !within(GenericServlet);

    Object around():
    	genericServletInit() {
	    LOGGER.fine("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    LOGGER.info("[] not weaved.");
	    Object result = proceed();
	    return result;
	}

    /** To weave in each Servlet Destroy */
    public pointcut genericServletDestory() :
    	execution(void GenericServlet+.destroy());// && !within(GenericServlet);

    Object around():
    	genericServletDestory() {
	    LOGGER.fine("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    LOGGER.info("[] not weaved.");
	    Object result = proceed();
	    return result;
	}

}
