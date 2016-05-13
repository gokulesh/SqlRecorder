package org.sqlrecorder.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlrecorder.util.TestUtils;
import org.testng.annotations.Test;

//TODO : Refactor the code to validate lines in the log files. It  uses the same code in all the tests. 

@Test
public class SqlRecorderDriverIntegrationTest {

    private Connection connection;
    private final String jdbcUrl = "jdbc:hsqldb:mem:mymemdb";
    private final String username = "SA";
    private final String password = "";
    private final String queryFile = "/tmp/query.log";
    
    public void canObtainConnection()  throws SQLException, ClassNotFoundException {
    	TestUtils.deRegisterAllDrivers();
        System.setProperty("sqlrecorder.config.location", "classpath:sampleconfig.xml");
        Class.forName("org.sqlrecorder.SqlRecorder");
        connection = DriverManager.getConnection(jdbcUrl, username, password);    	
    }
}
