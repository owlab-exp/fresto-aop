package fresto.aspects;

import javax.servlet.http.HttpServlet;

public aspect HttpServletMonitor {
    /** For any servlet request methods. */
    public pointcut monitoredOperation(Object operation) :
    	execution(void HttpServlet.do*(..)) && this(operation);
	
    /** Advice something for the monitored operatinos. */
    void around(Object operation) : 
    	monitoredOperation(operation) {
		System.out.println("Test sTime: " + getTime());

		proceed(operation);

		System.out.println("Test eTime: " + getTime());
    	}

    public long getTime() {
        return System.currentTimeMillis();
    }

}
