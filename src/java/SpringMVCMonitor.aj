package fresto.aspects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.FrameworkServlet;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


public aspect SpringMVCMonitor {

    private int step = 0;
    /** */
    public pointcut springController(HttpServletRequest request, HttpServletResponse response) :
    	execution(public ModelAndView Controller+.*(HttpServletRequest, HttpServletResponse)) && args (request, response);

    public pointcut springHandle(HttpServletRequest request, HttpServletResponse response) :
    	execution(public ModelAndView Controller+.*(HttpServletRequest, HttpServletResponse))
	&& execution(* handleRequest(HttpServletRequest, HttpServletResponse)) && args (request, response);
	//execution(* handleRequest(HttpServletRequest, HttpServletResponse)) && args (request, response);

    public pointcut springMultiAction(HttpServletRequest request, HttpServletResponse response) :
    	execution(public ModelAndView Controller+.*(HttpServletRequest, HttpServletResponse))
	//&& execution(* handleRequest(HttpServletRequest, HttpServletResponse)) && args (request, response);
	&& execution(* MultiActionController+.*(HttpServletRequest, HttpServletResponse)) && args (request, response);

    public pointcut springMultiActionInvokeMethodName(String methodName, HttpServletRequest request, HttpServletResponse response) :
	execution(* MultiActionController.invokeNameMethod(String, HttpServletRequest, HttpServletResponse)) && args (methodName, request, response);

    Object around(String methodName, HttpServletRequest request, HttpServletResponse response) :
    	springMultiActionInvokeMethodName(methodName, request, response) {
	    step++;
	    System.out.println("[" + step + "]" + "[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());
	    System.out.println("[" + step + "]" + "[methodName] " + methodName);

	    extractHttpServletRequestInfo(step, request);

	    Object result = proceed(methodName, request, response);

	    extractHttpServletResponseInfo(step, response);

	    step--;
	    return result;
	}

    Object around(HttpServletRequest request, HttpServletResponse response):
    	//springMultiAction(request, response) {
    	//springController(request, response) {
    	springHandle(request, response) {
	    step++;
	    System.out.println("[" + step + "]" + "[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    extractHttpServletRequestInfo(step, request);

	    Object result = proceed(request, response);

	    extractHttpServletResponseInfo(step, response);

	    step--;
	    return result;
	}
	

    public void extractHttpServletRequestInfo(int step, HttpServletRequest request) {
         System.out.println("[" + step + "]" + "[contextPath] " + request.getContextPath());
         System.out.println("[" + step + "]" + "[fresto-uuid] " + request.getHeader("fresto-uuid"));
         System.out.println("[" + step + "]" + "[requestURI] " + request.getRequestURI());
         System.out.println("[" + step + "]" + "[requestURL] " + request.getRequestURL().toString());
         System.out.println("[" + step + "]" + "[servletPath] " + request.getServletPath());
         System.out.println("[" + step + "]" + "[localName] " + request.getLocalName());
         System.out.println("[" + step + "]" + "[localPort] " + request.getLocalPort());
         System.out.println("[" + step + "]" + "[remoteAddr] " + request.getRemoteAddr());
    }

    public void extractHttpServletResponseInfo(int step, HttpServletResponse response) {
         System.out.println("[" + step + "]" + "[SC] " + response.getStatus());
    }
}
