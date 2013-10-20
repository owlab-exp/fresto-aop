package fresto.aspects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.FrameworkServlet;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.util.logging.Logger;

public aspect SpringMVCMonitor {
    private static Logger LOGGER = Logger.getLogger("SpringMVCMonitor");


    /** */
    public pointcut springControllerHandle() :
	//execution(ModelAndView handleRequest*(..));
	execution(* *.*Controller.*(..)) && !within(*.AbstractController);

    public pointcut springDomainService() :
	//execution(ModelAndView handleRequest*(..));
	execution(* *.*Service.*(..));

    public pointcut springHandleRequest(HttpServletRequest request, HttpServletResponse response) :
	execution(ModelAndView handleRequest*(HttpServletRequest, HttpServletResponse)) && args (request, response);
	//execution(* handleRequest(HttpServletRequest, HttpServletResponse)) && args (request, response);

    public pointcut springMultiAction(HttpServletRequest request, HttpServletResponse response) :
    	execution(public ModelAndView Controller+.*(HttpServletRequest, HttpServletResponse))
	//&& execution(* handleRequest(HttpServletRequest, HttpServletResponse)) && args (request, response);
	&& execution(* MultiActionController+.*(HttpServletRequest, HttpServletResponse)) && args (request, response);

    public pointcut springMultiActionInvokeMethodName(String methodName, HttpServletRequest request, HttpServletResponse response) :
	execution(* MultiActionController.invokeNameMethod(String, HttpServletRequest, HttpServletResponse)) && args (methodName, request, response);

    Object around() :
	springControllerHandle() {
	    LOGGER.info("[controllerHandler thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    FrestoTracker.beginTrack();
	    Object result = proceed();
	    FrestoTracker.endTrack();

	    return result;
	}

    Object around() :
	springDomainService() {
	    LOGGER.info("[domainService thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    FrestoTracker.beginTrack();
	    Object result = proceed();
	    FrestoTracker.endTrack();

	    return result;
	}

    Object around(String methodName, HttpServletRequest request, HttpServletResponse response) :
    	springMultiActionInvokeMethodName(methodName, request, response) {
	    LOGGER.info("[multiActionInvoke thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    FrestoTracker.beginTrack();
	    Object result = proceed(methodName, request, response);
	    FrestoTracker.endTrack();

	    return result;


	}

    Object around(HttpServletRequest request, HttpServletResponse response):
    	//springMultiAction(request, response) {
    	//springController(request, response) {
    	springHandleRequest(request, response) {
	    LOGGER.info("[handler thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    FrestoTracker.beginTrack();
	    Object result = proceed(request, response);
	    FrestoTracker.endTrack();

	    return result;
	}
	
}
