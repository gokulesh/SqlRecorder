package org.sqlrecorder;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.sqlrecorder.exception.SQLRecorderException;
import org.sqlrecorder.util.LogUtils;

public final class SqlRecorderDriver implements Driver {

    private static final Logger LOG = LogUtils.loggerForThisClass();
    private static SqlRecorder recorder;
    static {
        try {
            init();
        } catch (SQLException e) {
            throw new SQLRecorderException("Unable to register SqlRecorder driver", e);
        }
    }

    private static void init() throws SQLException {
        String configFileLocation = System.getProperty("sqlrecorder.config.location");
        if (StringUtils.isNotBlank(configFileLocation)) {
        	ApplicationContext ac = new ClassPathXmlApplicationContext(configFileLocation);
        	recorder = ac.getBean( "sqlRecorder", SqlRecorder.class);
        	
        }
    }

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		return recorder.connect(url, info);
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return recorder.acceptsURL(url);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return recorder.getPropertyInfo(url, info);
	}

	@Override
	public int getMajorVersion() {
		return recorder.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return recorder.getMinorVersion();
	}

	@Override
	public boolean jdbcCompliant() {
		return recorder.jdbcCompliant();
	}
 
}
