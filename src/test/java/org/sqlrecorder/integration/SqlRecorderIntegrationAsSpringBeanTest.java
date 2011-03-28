package org.sqlrecorder.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.StringContains.containsString;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.sqlrecorder.SqlRecorder;
import org.sqlrecorder.events.listener.FileOutputListener;
import org.sqlrecorder.events.listener.StatementListener;
import org.sqlrecorder.util.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TODO This is a temporary test to see how this works. Needs to be merged with the main integration test
 *
 * @see SqlRecorderIntegrationTest
 */

@Test
public class SqlRecorderIntegrationAsSpringBeanTest {

    private Connection connection;
    private final String jdbcUrl = "jdbc:hsqldb:mem:mymemdb";
    private final String username = "SA";
    private final String password = "";
    private final String queryFile = "/tmp/query.log";
    private String driverClass = "org.hsqldb.jdbc.JDBCDriver";

    @BeforeClass
    public void setUp() throws SQLException, ClassNotFoundException {

        TestUtils.deRegisterAllDrivers();
        Class.forName(driverClass);
        new SqlRecorder(driverClass, Lists.<StatementListener>newArrayList(new FileOutputListener(queryFile)));
        connection = DriverManager.getConnection(jdbcUrl, username, password);
        Statement s = connection.createStatement();
        s.executeUpdate("create table sqlrecuser(name varchar(20),age int,postcode smallint)");
        s.execute("insert into sqlrecuser values('gokul',20,3000) ");
        connection.commit();
        connection.close();
    }

    @AfterClass
    public void deleteFile() throws SQLException {
        File f = new File("/tmp/query.log");
        if (f.exists()) {
            f.delete();
        }
        connection = DriverManager.getConnection(jdbcUrl, username, password);
        connection.createStatement().execute("SHUTDOWN");
    }

    @BeforeMethod
    public void setupConnection() throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public void validateStatementExecution() throws SQLException, IOException {
        List<String> linesBeforeExecution = returnLinesFromFile();

        Statement s = connection.createStatement();
        String query = "select name from sqlrecuser";
        s.executeQuery(query);
        List<String> linesAfterExecution = returnLinesFromFile();

        int lineCount = linesAfterExecution.size() - linesBeforeExecution.size();
        assertThat(lineCount, equalTo(1));
        assertThat(linesAfterExecution.get(linesAfterExecution.size() - 1), containsString(query));
    }

    public void validateRegisteredDrivers(){
        List<Driver> alldrivers= Collections.list(DriverManager.getDrivers());
        assertThat(alldrivers.size(),equalTo(1));
    }

    @AfterMethod
    public void tearDown() throws SQLException {
        connection.close();
    }

    private List<String> returnLinesFromFile() {
        File f = new File(queryFile);
        List<String> lines = null;
        try {
            lines = Files.readLines(f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException("Error reading log file");
        }
        return lines;
    }
}
