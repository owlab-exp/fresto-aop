package fresto.aspects;
public aspect DAOAspect extends AbstractLoggingAspect {

	public pointcut daoCall():
		  // packages in Dao layer to weave
		 execution(public * com.brimllc.dao..*DaoImpl.*(..));		

	// exclude the calls to methods in the exclude list
	before() : daoCall() && exclude(){
		doBefore(thisJoinPointStaticPart.getSignature());
	}

	after() : daoCall() && exclude(){
		doAfter(thisJoinPointStaticPart.getSignature());
	}
}
