package fresto.aspects;

import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.aspectj.lang.Signature;

public abstract class AbstractLoggingAspect {
  // each HttpRequest is captured in this threadLocal to get access to request cookies and HttpSession
      protected static ThreadLocal myCurrentRequest = new ThreadLocal();
      protected static final String CURRENT_REQUEST_SEQUENCE_NUMBER = "CURRENT_REQUEST_SEQUENCE_NUMBER";
      protected static int MAX_REQUEST_NUMBER = 100000;
      // you dont want to monitor request for images, javascript etc, so lets ignore them
      protected static final Pattern PATTERN = Pattern.compile(".*/view/help/.*|.*/js/.*|.*/compressedJs/.*|.*/dynamicImage/.*|.*/images/.*|.*/css/.*|.*/a4j/.*");

      // here is the nuts and bolts, which does the calculation. Method A is the entry point, goes on the stack. A calls B,
      B goes on the stack over A, so the method just below is always the parent of the current method and so on. As each method ends execution, it pops from the stack.
      protected static ThreadLocal<Stack> myMethodCallStack = new ThreadLocal<Stack>(){
      	protected synchronized Stack initialValue(){
      		return new Stack();
      	}
      };	

      /**
       * Pointcut to exclude some packages. You dont want some methods defined which falls in the same packages, and will
         match the pointcut, like EJB initilialization or MDB's onMessage which does not execute synchronously. You can get rid of it
     if you wish.
       */
      public pointcut exclude():
      	// do not include the aspects itself. Make sure the package of aspects is included,
      	// else it will cause an infinite loop while weaving.
      	!within(com.brimllc.aspects..*)
         	// do not weave EJB container callbacks
      	&& ! execution(* *.ejb*(..))
      	&& ! execution(* *.setSessionContext(..))
      	// do not weave JMS beans callbacks
      	&& ! execution(* *.onMessage(..))
      	&& ! cflowbelow(execution(* *.onMessage(..)))
      	// do not weave any method which starts with WL
      	&& ! execution(* *.__WL*(..))
      	// do not include any exceptions
      	&& ! within(java.lang.Exception+)
      	&& !cflow(ping());

      /**
       * This gets the information from the request
       * @param parameter
       * @return
       */
      protected String getRequestInfo(HttpServletRequest request , String parameter){
      	Cookie[] cookies = null;
      	if(request.getRequestURI() == null || request.getRequestURI().length() == 0){
      		logger.logDebug(" Request generated null pointer");
      	}
      	try{
      		cookies = request.getCookies();
      	}catch(Exception ex){
      		cookies = null;

      	}
      	if(cookies == null || cookies.length == 0){
      		return null;
      	}

      	String paramValue = null;
      	for(Cookie c : cookies) {
             if(c.getName().equals(parameter)) {
          	   paramValue = c.getValue().replaceAll("%3A",":");
          	   break;
             }
          }
      	return paramValue;
      }

      /**
       * Factory method to create ExecutionRecord
       * @return
       */
      protected ExecutionRecord createMethodExecutionRecord(){
      	return new ExecutionRecord();
      }

      /**
       * Updates the record with information from the request
       * @param record
       * @param signature
       * @return
       */
      protected ExecutionRecord updatedMethodExecutionRecord(HttpServletRequest request, ExecutionRecord record, Signature signature){
      	if (request.getHeader("Proxy-Client-IP") == null){
      		record.setClientIP(request.getRemoteAddr());
      	} else {
      		record.setClientIP(request.getHeader("Proxy-Client-IP"));
      	}
      	record.setRequestURI(request.getRequestURI());
      	record.setSequenceNumber(getSequenceNumber(request));
      	record.setUserId(getRequestInfo(request, "USERID"));
      	record.setSessionId(getRequestInfo(request, "JSESSIONID"));
      	record.setUseCaseScenario(getRequestInfo(request, "sfowTestCase"));
      	record.setSuiteName(getRequestInfo(request, "testSuiteName"));
      	record.setExecutionPath(getRequestInfo(request, "testSuiteCategory"));
      	record.setPackageName(signature.getDeclaringType().getPackage().getName());
      	record.setMethodName(signature.getDeclaringTypeName()+"."+signature.getName());
      	record.setStartExecutionTime(System.currentTimeMillis());
      	// check the top of the stack, thats the parent method which called this method
      	ExecutionRecord parentRecord = myMethodCallStack.get().size() > 0 ? myMethodCallStack.get().peek() : null;
      	if(parentRecord != null){
      		record.setParentMethod(parentRecord.getMethodName());
      		parentRecord.setHasChildren(true);
      	}
      	return record;
      }

      /**
       * Adds a record for method execution on the stack
       * @param obj
       */

      public void doBefore(Signature signature)
      {
      	// no need to record till the request is sent to server
      	HttpServletRequest request = myCurrentRequest.get();
      	if(request == null){
      		return;
      	}
      	// if the request is not a valid request then dont log
      	if(!isValidRequest(request)){
      		return;
      	}
      	// create a record
      	ExecutionRecord record = createMethodExecutionRecord();
      	// populate the record with information from request and method info
      	updatedMethodExecutionRecord(request, record, signature);
      	// push the record on top of the stack
      	myMethodCallStack.get().push(record);
    }

      /**
       * Pop the method record from the stack,
       * add the end time of method
       * calculate the total time executed for this method	 *
       * @param obj
       */
      public void doAfter(Signature signature)
      {
      	// no need to record till the request is sent to server
      	HttpServletRequest request = myCurrentRequest.get();
      	if(request == null){
      		return;
      	}
      	// if the request is not a valid request then dont log
      	if(!isValidRequest(request)){
      		return;
      	}
      	// pop the method from method call stack
      	ExecutionRecord record =  myMethodCallStack.get().size() > 0 ? myMethodCallStack.get().pop() : null;
      	if(record == null){
      		return;
      	}
      	// update the end time for method call
      	record.setEndExecutionTime(System.currentTimeMillis());
      	if(record.getTotalExecutionTime() > 0){
      		// now let the parents know I am done
      		incrementParentsChildrenExecutionTime(record);
      	}

      	// log this record to the log file
      	LogUtils.logTiming(record);
      }

      /**
       * This method increments the childExecutionTime in the immediate parent record.
       * Every method on the stack is still executing. Method on stack top is the current executing method (smallest child),
       * while all others are its parents and waiting for it to finish to resume.
       * When child method is finished executing, its execution time can be deducted from parent's execution time.
       * * @param childRecord
       */
      protected void incrementParentsChildrenExecutionTime(ExecutionRecord childMethodRecord){
      	// increment the childExecutionTime of the immediate parent with childMethodRecord's total
      	// execution time.
      	ExecutionRecord parentRecord = myMethodCallStack.get().size() > 0 ? myMethodCallStack.get().peek() : null;
      	if(parentRecord == null){
      		return;
      	}
      	parentRecord.setChildrenTotalExecutionTime(parentRecord.getChildrenTotalExecutionTime() + childMethodRecord.getTotalExecutionTime());
      }		

       protected boolean isValidRequest(HttpServletRequest request){
      	 if(request == null){
      		 return false;
      	 }

      	 String requestURI = request.getRequestURI();
      	 if(requestURI == null || requestURI.trim().length() == 0){
      		 return false;
      	 }

      	// exclude any request for image, css or javascript
      	 Matcher excludeMatcher = PATTERN.matcher(requestURI);
      	 if(excludeMatcher.matches()){
      		 return false;
      	 }

      	 return true;
       }

        // Other helper methods, no magic here
       public synchronized int getSequenceNumber(HttpServletRequest request){
      	 if(request.getSession(false) == null){
      		 return 1;
      	 }
      	 Object currentReqSequenceObj = request.getSession(false).getAttribute(CURRENT_REQUEST_SEQUENCE_NUMBER);
      	 if(currentReqSequenceObj == null){
      		 return 1;
      	 }
      	 return ((Integer)currentReqSequenceObj).intValue();
       }

       public synchronized void incrementSequenceNumber(HttpServletRequest request){
      	 if(request.getSession(false) == null){
      		 return;
      	 }
      	 int currentReqSequence = getSequenceNumber(request);
      	 if(currentReqSequence < MAX_REQUEST_NUMBER){
      		 currentReqSequence++;
      	 }else{
      		 currentReqSequence = 1;
      	 }

		 request.getSession(false).setAttribute(CURRENT_REQUEST_SEQUENCE_NUMBER, new Integer(currentReqSequence));
	 }
}
