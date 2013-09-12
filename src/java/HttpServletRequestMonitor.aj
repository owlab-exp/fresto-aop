package fresto.aspects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.web.servlet.FrameworkServlet;

public aspect HttpServletRequestMonitor {

    private int step = 0;
    /** In case of Spring Framework application, all servlet request handled by FrameworkServler */
    public pointcut frameworkServletService(HttpServletRequest request, HttpServletResponse response) :
    	execution(void FrameworkServlet.service(HttpServletRequest, HttpServletResponse)) && args (request, response);

    Object around(HttpServletRequest request, HttpServletResponse response):
    	frameworkServletService(request, response) {
	    step++;
	    System.out.println("[" + step + "]" + "[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    extractHttpServletRequestInfo(step, request);

	    Object result = proceed(request, response);

	    extractHttpServletResponseInfo(step, response);

	    step--;
	    return result;
	}
	
    /** To Capture JSP page call */
    public pointcut httpJspPageService(HttpServletRequest request, HttpServletResponse response) :
         execution(void HttpJspPage._jspService(HttpServletRequest, HttpServletResponse)) && args(request, response); 
     
    
    Object around(HttpServletRequest request, HttpServletResponse response) :
    	httpJspPageService(request, response) {
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
