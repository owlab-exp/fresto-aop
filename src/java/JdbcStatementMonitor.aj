package fresto.aspects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Map;

import java.util.logging.Logger;
//import edu.emory.mathcs.util.WeakIdentityHashMap;

public aspect JdbcStatementMonitor {
    private static Logger LOGGER = Logger.getLogger("JdbcStatementMontior");

    public pointcut statementExecute(String sql) :
    	call(ResultSet+ java.sql.Statement.execute*(String)) && args(sql);

    Object around(String sql) :
    	statementExecute(sql) {
	    LOGGER.info("[sql] " + sql);
	    Object result = proceed(sql);
	    return result;
	}
    /** Match JDBC statements */
    public pointcut statementExec(Statement statement) :
    	call(* java.sql..*.execute*(..)) && target(statement);

    /**
     * Store the sanitized SQL for dynamic statements.
     */
    before(Statement statement, String sql): statementExec(statement) && args(sql, ..) {
	//sql = stripAfterWhere(sql);
	//setUpStatement(statement, sql);
	LOGGER.info("[SQL] " + sql);
    }

    after(Statement statement, String sql): statementExec(statement) && args(sql, ..) {
	//sql = stripAfterWhere(sql);
	//setUpStatement(statement, sql);
	LOGGER.info("[SQL] " + sql);
    }

    //public pointcut springJdbcOperations(String statement) :
    //    execution(* org.springframework.jdbc.core.JdbcOperations.*(String, ..)) && args(statement);

    //Object around(String statement) : springJdbcOperations(statement) {
    //    LOGGER.info("called");
    //    LOGGER.info("[statement] " + statement);

    //    Object result = proceed(statement);

    //    return result;
    //}


    ///** Probe JDBC statement executions */
    //Object around(final Statement statement) : statementExec(statement) {
    //    RequestContext requestContext = new StatementRequestContext() {
    //        public Object doExecute() {
    //    	curStatement = statement;
    //    	return proceed(statement);
    //        }
    //        public Object doSomething() {
    //    	return null;
    //        }
    //        protected String getRequestType() {return "execute"; }
    //    };

    //    return requestContext.execute();
    //}

    ///** Call to create a Statement */
    //public pointcut callCreateStatement(Connection connection) :
    //	call(Statement+ Connection.*(..)) && target(connection);
    //    
    //after(Connection connection) returning (Statement statement) : callCreateStatement(connection) {
    //    synchronized(JdbcStatementMonitor.this) {
    //        statementCreators.put(statement, connection);
    //    }
    //}
    ///** Call to prepare a Statement */
    //public pointcut callCreatePreparedStatement(String sql) :
    //	call(PreparedStatement+ Connection.*(String, ..)) && args(sql, ..);

    ///** Do someting in preparing statements */
    //Object around(final String sql) : callCreatePreparedStatement(sql) {
    //    RequestContext requestContext = new StatementRequestContext() {
    //        public Object doExecute() {
    //    	curStatement = (PreparedStatement)proceed(sql);
    //    	System.out.println(sql);
    //    	setUpStatement(curStatement, sql);
    //    	return curStatement;
    //        }
    //        public Object doSomething() {
    //    	return null;
    //        }
    //        protected String getRequestType() {return "prepare";}
    //    };
    //    return requestContext.execute();
    //}

    //protected abstract class StatementRequestContext extends RequestContext {
    //    public Statement curStatement;
    //    protected abstract String getRequestType();

    //    ///** Find statistics for this statement, looking for its SQL string in the parent request's statistics context */
    //    //        protected PerfStats lookupStats() {
    //    //            if (getParent() != null) {
    //    //                Connection connection = null;
    //    //                String sql = null;
    //    //
    //    //                synchronized (JdbcStatementMonitor.this) {
    //    //                    connection = (Connection) statementCreators.get(curStatement);
    //    //                    sql = (String) statementSql.get(curStatement);
    //    //                }
    //    //
    //    //                if (connection != null) {
    //    //                    String databaseName = JdbcConnectionMonitor.aspectOf().getDatabaseName(connection);
    //    //                    if (databaseName != null && sql != null) {
    //    //                        OperationStats opStats = (OperationStats) getParent().getStats();
    //    //                        if (opStats != null) {
    //    //                            ResourceStats dbStats = opStats.getResourceStats(databaseName);
    //    //
    //    ////                          // if (isDebugEnabled()) { logDebug("looking up stats for "+getRequestType()+"^"+sql); }
    //    //
    //    //                            //better:
    //    //                            //return dbStats.getRequestStats(sql).getRequestStats(getRequestType());
    //    //                            return dbStats.getRequestStats(sql+"^"+getRequestType());
    //    //                        }
    //    //                    }
    //    //                }
    //    //            }
    //    //            return null;
    //    //        }
    //    //
    //    //
    //}

    //private synchronized void setUpStatement(Statement statement, String sql) {
    //    statementSql.put(statement, sql);
    //}

    //private Map statementCreators = new WeakIdentityHashMap();

    //private Map statementSql = new WeakIdentityHashMap();
}
