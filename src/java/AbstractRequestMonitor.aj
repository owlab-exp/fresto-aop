public abstract aspect AbstractRequestMonitor {
    public pointcut requestExecution(RequestContext requestContext) : execution(* RequestContext.execute(..)) && this(requestContext);

    public pointcut inRequest(RequestContext requestContext) : cflow(requestExecution(requestContext));

    /**
     * parent relationships
     */
    after(RequestContext parentContext) returning(RequestContext childContext): call(RequestContext+.new(..)) && inRequest(parentContext) {
	childContext.setParent(parentContext);
    }

    public long getTime() {
	return System.currentTimeMillis();
    }

    /** 
     * to hold context information
     */
    public abstract class RequestContext {
	protected RequestContext parent = null;
	protected long startTime;

	public final Object execute() {

	    System.out.println("execute1");

	    Object result = doExecute();

	    System.out.println("execute2");

	    return result;
	}

	public abstract Object doExecute();

	public RequestContext getParent() {
	    return this.parent;
	}

	public void setParent(RequestContext parent) {
	    this.parent = parent;
	}
    }
}
