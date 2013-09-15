package fresto.aspects;

public aspect DAOMonitor {

    /** Daos in jpetstoreLA */
    public pointcut daoOperation() :
    	execution(public * org.springframework.samples.jpetstore..*Dao.*(..))
	&& !execution(public * org.springframework.samples.jpetstore..*Dao.setSequenceDao(..));

    Object around():
    	daoOperation() {
	    System.out.println("[thisJoinPoint.getSignature] " + thisJoinPoint.getSignature());

	    FrestoTracker.beginTrack();

	    Object result = proceed();

	    // Just after proceed
	    FrestoTracker.endTrack();

//	    workOnResponse(response);

	    return result;
	}
}
