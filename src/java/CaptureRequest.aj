package fresto.aspects;
/**
 * This aspect captures the HttpRequest in a ThreadLocal variable.
 * The idea is to capture the request from the first filter the request hits.
 *
 */

 import javax.servlet.http.HttpServletRequest;

public aspect CaptureRequest extends AbstractLoggingAspect {

	public pointcut entireExecutionOfOneRequest() :
		execution(public void doFilter(..));

	 before() : entireExecutionOfOneRequest()
	    {

	      // capture the request from the doFilter method arguments
		  Object[] args = thisJoinPoint.getArgs();
	    	HttpServletRequest request = (HttpServletRequest)args[0];
			// check if the request is not an img/css/javascript request
	    	if(isValidRequest(request)){
		    	synchronized(this){
		    		HttpServletRequestWrapper reqWrapper = new HttpServletRequestWrapper(request);
					// set this request to the threadLocal defined in AbstractLoggingAspect
	    			myCurrentRequest.set(reqWrapper);
	    			incrementSequenceNumber(request);

		    	}
		    	// keep record, calls the AbstractLoggingAspect (the skeleton) passing the method signature
		    	doBefore(thisJoinPointStaticPart.getSignature());
	    	}

	    }	 

	    // since this is the entry point for the request, some more stuff is needed here, like clearing the Thread-Local,
		// the method stack used for calculation is reset.
	    after() : entireExecutionOfOneRequest()
	    {
	    	// process only when request is valid
	    	 // capture the request
		      Object[] args = thisJoinPoint.getArgs();
		      HttpServletRequest request = (HttpServletRequest)args[0];
		      if(isValidRequest(request)){
		    	doAfter(thisJoinPointStaticPart.getSignature());
		      }
		      // now get rid of request and any methods lying on the stack
		     myCurrentRequest.set(null);
		     myMethodCallStack.set(new Stack());
	     }

}
