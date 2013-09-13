package fresto.aspects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.FrameworkServlet;

public aspect HttpServletRequestMonitor {

    /** In case of Spring Framework application, all servlet request handled by FrameworkServler */
    public pointcut frameworkServletService(HttpServletRequest request, HttpServletResponse response) :
    	execution(void FrameworkServlet.service(HttpServletRequest, HttpServletResponse)) && args (request, response);

    Object around(HttpServletRequest request, HttpServletResponse response):
    	frameworkServletService(request, response) {
	    System.out.println("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    workOnRequest(request);

	    Object result = proceed(request, response);

	    workOnResponse(response);

	    return result;
	}
	
    /** To Capture JSP page call */
    public pointcut httpJspPageService(HttpServletRequest request, HttpServletResponse response) :
         execution(void HttpJspPage._jspService(HttpServletRequest, HttpServletResponse)) && args(request, response); 
     
    
    Object around(HttpServletRequest request, HttpServletResponse response) :
    	httpJspPageService(request, response) {
             System.out.println("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

             workOnRequest(request);

             Object result = proceed(request, response);

             workOnResponse(response);

             return result;
         }

    public void workOnRequest(HttpServletRequest request) {
         System.out.println("[contextPath] " + request.getContextPath());
         System.out.println("[fresto-uuid] " + request.getHeader("fresto-uuid"));
         System.out.println("[requestURI] " + request.getRequestURI());
         System.out.println("[requestURL] " + request.getRequestURL().toString());
         System.out.println("[servletPath] " + request.getServletPath());
         System.out.println("[localName] " + request.getLocalName());
         System.out.println("[localPort] " + request.getLocalPort());
         System.out.println("[remoteAddr] " + request.getRemoteAddr());

	 FrestoContext fc = FrestoContext.createInstance(request.getHeader("fresto-uuid"));
	 fc.increaseDepth();
	 FrestoTracker.set(fc);
    }

    public void workOnResponse(HttpServletResponse response) {
         System.out.println("[SC] " + response.getStatus());

	 FrestoTracker.get().decreaseDepth();
	 System.out.println("[Depth] " + FrestoTracker.get().getDepth());
	 if(FrestoTracker.get().getDepth() == -1) {
	     FrestoTracker.unset();
	 }
    }
}
