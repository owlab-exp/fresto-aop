package fresto.aspects;
/**
 * This record keeps the information in a ThreadLocal for each request. A parser
 * can later parse this information. The idea is if method A calls B, B calls C, and C calls D, then this will record
   the time it took only in that method. Like, A took 50 milli seconds in whole, and B took 30 millis, so the time spent in
   method A is just 20 millis.
 *
 */
public class ExecutionRecord {

	// client IP address, can be fetched from request header
	private String myClientIP;

	// request sequence, a single request processing can call multiple methods. Used for reporting
	private int myRequestSequenceNumber;

	// user who invoked the request, fetched from cookie or request session, for reporting
	private String myUserId;

	// session id to match the same, used for reporting
	private String mySessionId;

	// use case scenario, can be fetched from cookie, used for reporting
	private String myUseCaseScenario;

	// use case suite, selenium or canoe suite name
	private String mySuiteName;

	// execution path
	private String myExecutionPath;

	// request uri, used for reporting
	private String myRequestURI;

	// package name, identify the layer in which this method resides
	private String myPackageName;

	// method name
	private String myMethodName;

	// method execution count
	private int myMethodExecutionCount;

	// start time
	protected long myStartExecutionTime;

	// end time
	protected long myEndExecutionTime;

	// total execution time
	protected long myTotalExecutionTime;

	// parent method
	private String myParentMethod;

	// has children flag
	private boolean myHasChildren = false;

	// total execution time of children
	private long myChildrenTotalExecutionTime;

	public ExecutionRecord() {

	}

	public ExecutionRecord(String userId, String sessionId, String useCase,
			String packageName, String method, int executionCount,
			long startTime, long endTime) {
		this.myUserId = userId;
		this.mySessionId = sessionId;
		this.myUseCaseScenario = useCase;
		this.myPackageName = packageName;
		this.myMethodName = method;
		this.myMethodExecutionCount = executionCount;
		this.myStartExecutionTime = startTime;
		this.myEndExcecutionTime = endTime;
	}

	public String getUserId() {
		return myUserId;
	}

	public void setUserId(String myUserId) {
		this.myUserId = myUserId;
	}

	// other getter and setters for the properties ...........

	public long getTotalExecutionTime(){
		this.myTotalExecutionTime = this.myEndExcecutionTime - this.myStartExecutionTime;
		return this.myTotalExecutionTime;
	}

	public long getExactExecutionTime() {
		return this.myEndExecutionTime - this.myStartExecutionTime - this.myChildrenTotalExecutionTime;
	}

	// the method in whose scope this method is getting called.
	public String getParentMethod() {
		return myParentMethod;
	}

	public void setParentMethod(String myParentMethod) {
		this.myParentMethod = myParentMethod;
	}

	// overridden toString to dump to the log file.
	public String toString() {
		StringBuffer buff = new StringBuffer(100);
		buff.append(this.getClientIP()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getSequenceNumber())
		.append("."+Thread.currentThread().getId()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getUserId())
				.append(PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getSessionId()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getUseCaseScenario()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getSuiteName()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getExecutionPath()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getRequestURI()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getPackageName()).append(
				PerformanceLogger.COLUMN_SEPARATOR);
		buff.append(this.getMethodName()).append(PerformanceLogger.
		 COLUMN_SEPARATOR);
		// buff.append(this.getParentMethod()).append(PerformanceLogger.
		//		 COLUMN_SEPARATOR);
		buff.append(this.getStartExecutionTime()).append(PerformanceLogger.
				 COLUMN_SEPARATOR);
		buff.append(this.getEndExecutionTime()).append(PerformanceLogger.
				 COLUMN_SEPARATOR);
		// For debugging purposes only, enable this for each method's cumulative execution time
		// Also enable the Performance Logger to add another column in the file for Cumulative Execution time
		//buff.append(this.getTotalExecutionTime()()).append(PerformanceLogger.
		//		 COLUMN_SEPARATOR);
		buff.append(this.getExactExecutionTime());

		return buff.toString();
	}

	public boolean hasChildren() {
		return myHasChildren;
	}

	public void setHasChildren(boolean myHasChildren) {
		this.myHasChildren = myHasChildren;
	}

	public long getChildrenTotalExecutionTime() {
		return myChildrenTotalExecutionTime;
	}

	public void setChildrenTotalExecutionTime(long myChildrenTotalExecutionTime) {
		this.myChildrenTotalExecutionTime = myChildrenTotalExecutionTime;
	}

	public void setStartExecutionTime(long startExecutionTime) {
		this.myStartExecutionTime = startExecutionTime;
	}

	public long getStartExecutionTime() {
		return this.myStartExecutionTime;
	}

	public void setEndExecutionTime(long endExecutionTime) {
		this.myEndExecutionTime = endExecutionTime;
	}

	public long getEndExecutionTime() {
		return this.myEndExecutionTime;
	}

	public void setMethodName(String methodName) {
		this.myMethodName = methodName;
	}

	public String getMethodName() {
		return this.myMethodName;
	}
}
