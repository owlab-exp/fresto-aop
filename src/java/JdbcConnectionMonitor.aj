package fresto.aspects;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import javax.sql.DataSource;

public aspect JdbcConnectionMonitor {
    private static Logger LOGGER = Logger.getLogger("JdbcConnectionMonitor");

    public pointcut dataSourceConnectionCall(DataSource dataSource) :
    	call(Connection+ DataSource.getConnection(..)) && target(dataSource);

    public pointcut directConnectionCall(String url) :
    	(call(Connection+ Driver.connect(..)) || call(Connection+ DriverManager.getConnection(..)) ) && args(url, ..);
    
    public pointcut nestedConnectionCall() :
    	cflowbelow(dataSourceConnectionCall(*) || directConnectionCall(*));

    Connection around(final DataSource dataSource) :
    	dataSourceConnectionCall(dataSource) && !nestedConnectionCall() {

	    LOGGER.info("[dataSource] " + "accessed");
	    LOGGER.info("[dataSource] " + getNameForDataSource(dataSource));

	    Connection connection = proceed(dataSource);

	    return connection;
	}

    Connection around(final String url) : 
    	directConnectionCall(url) && !nestedConnectionCall() {
	    LOGGER.info(url);

	    Connection connection = proceed(url);

	    return connection;
    	}

    public String getNameForDataSource(DataSource dataSource) {
	String possibleNames[] = {
	    "getDatabaseName",
	    "getDatabasename",
	    "getUrl",
	    "getURL",
	    "getDataSourceName",
	    "getDescription"
	};

	String name = null;
	for(String possibleName : possibleNames) {
	    try {
	    Method method = dataSource.getClass().getMethod(possibleName, null);
	    name = (String) method.invoke(dataSource, null);
	    } catch(Exception e) {
		//
	    }
	}

	return (name != null) ? name : "unknown";
    }
}
