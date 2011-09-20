package org.sqlrecorder.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.StringContains.containsString;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.sqlrecorder.util.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.Files;

//TODO : Refactor the code to validate lines in the log files. It  uses the same code in all the tests. 

@Test
public class SqlRecorderIntegrationTest {

    private Connection connection;
    private final String jdbcUrl = "jdbc:hsqldb:mem:mymemdb";
    private final String username = "SA";
    private final String password = "";
    private final String queryFile = "/tmp/query.log";

    @BeforeClass
    public void setUp() throws SQLException, ClassNotFoundException {
        TestUtils.deRegisterAllDrivers();
        //System.setProperty("sqlrecorder.config.location", "classpath:sampleconfig.xml");
        //Class.forName("org.sqlrecorder.SqlRecorder");
        ClassPathXmlApplicationContext c = new ClassPathXmlApplicationContext("sampleconfig.xml");

        connection = DriverManager.getConnection(jdbcUrl, username, password);
        Statement s = connection.createStatement();
        s.executeUpdate("create table sqlrecuser(name varchar(20),age int,postcode smallint, created_date date)");
        s.execute("insert into sqlrecuser values('gokul',20,3000, null) ");
        connection.commit();
        s.execute("CREATE PROCEDURE sp1() LANGUAGE JAVA NO SQL EXTERNAL NAME 'CLASSPATH:org.sqlrecorder.storedprocedure.HsqlDbStoredProcedure.spWithoutParams'");
        s.execute("CREATE PROCEDURE sp2(IN name varchar(20),IN age int, IN createDate double) LANGUAGE JAVA NO SQL EXTERNAL NAME 'CLASSPATH:org.sqlrecorder.storedprocedure.HsqlDbStoredProcedure.spWithParams'");

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

    public void validateBatchStatementExecution() throws SQLException {
        List<String> linesBeforeExecution = returnLinesFromFile();

        Statement s = connection.createStatement();
        String query1 = "insert into sqlrecuser (name, age,postcode) values ('gokul',22,3012)";
        String query2 = "insert into sqlrecuser (name, age,postcode) values ('gokul',23,3050)";

        s.addBatch(query1);
        s.addBatch(query1);
        s.addBatch(query2);

        s.executeBatch();
        List<String> linesAfterExecution = returnLinesFromFile();

        int lineCount = linesAfterExecution.size() - linesBeforeExecution.size();
        assertThat(lineCount, equalTo(3));
        int zeroIndex = linesBeforeExecution.size();
        String firstQuery = linesAfterExecution.get(zeroIndex);
        String secondQuery = linesAfterExecution.get(zeroIndex + 1);
        String thirdQuery = linesAfterExecution.get(zeroIndex + 2);
        assertThat(firstQuery, containsString(query1));
        assertThat(secondQuery, containsString(query1));
        assertThat(thirdQuery, containsString(query2));
    }

    public void validatePreparedStatement() throws SQLException {
        List<String> linesBeforeExecution = returnLinesFromFile();

        PreparedStatement pstmt = connection.prepareStatement("select * from sqlrecuser where name = ? and age = ? and  postcode = ?");
        pstmt.setString(1, "gokul");
        pstmt.setInt(2, 20);
        pstmt.setInt(3, 3000);
        ResultSet rs = pstmt.executeQuery();
        int count = 0;
        while (rs.next()) {
            assertThat(rs.getString("name"), is("gokul"));
            assertThat(rs.getInt("age"), is(20));
            assertThat(rs.getInt("postcode"), is(3000));
            count++;
        }
        assertThat("should have returned at least 1 row", count, is(1));

        pstmt.executeQuery();
        List<String> linesAfterExecution = returnLinesFromFile();

        int lineCount = linesAfterExecution.size() - linesBeforeExecution.size();
        String expectedQuery = "select * from sqlrecuser where name = 'gokul' and age = 20 and  postcode = 3000";
        assertThat(lineCount, equalTo(2));
        assertThat(linesAfterExecution.get(linesAfterExecution.size() - 1), containsString(expectedQuery));
        assertThat(linesAfterExecution.get(linesAfterExecution.size() - 2), containsString(expectedQuery));
    }

    public void validateBatchPreparedStatement() throws SQLException {
        List<String> linesBeforeExecution = returnLinesFromFile();

        PreparedStatement pstmt = connection.prepareStatement("insert into sqlrecuser (name, age,postcode, created_date) values (?,?,?,?)");
        pstmt.setString(1, "gokul");
        pstmt.setInt(2, 50);
        pstmt.setInt(3, 3000);
        pstmt.setDate(4, new Date((new java.util.Date(11, 2, 1)).getTime()));
        pstmt.addBatch();
        pstmt.addBatch();

        pstmt.setInt(2, 40);
        pstmt.setInt(3, 2000);
        pstmt.setTimestamp(4, new java.sql.Timestamp((new java.util.Date(11, 2, 1)).getTime()));
        pstmt.addBatch();

        pstmt.executeBatch();

        List<String> linesAfterExecution = returnLinesFromFile();
        int lineCount = linesAfterExecution.size() - linesBeforeExecution.size();
        assertThat(lineCount, equalTo(3));

        int zeroIndex = linesBeforeExecution.size();
        String query1 = "insert into sqlrecuser (name, age,postcode, created_date) values ('gokul',50,3000,'1911-03-01')";
        String query2 = "insert into sqlrecuser (name, age,postcode, created_date) values ('gokul',40,2000,'1911-03-01 00:00:00.0')";

        String firstQuery = linesAfterExecution.get(zeroIndex);
        String secondQuery = linesAfterExecution.get(zeroIndex + 1);
        String thirdQuery = linesAfterExecution.get(zeroIndex + 2);
        assertThat(firstQuery, containsString(query1));
        assertThat(secondQuery, containsString(query1));
        assertThat(thirdQuery, containsString(query2));
    }

    public void storedProcedureWithNoParam() throws SQLException {
        List<String> linesBeforeExecution = returnLinesFromFile();

        CallableStatement cstmt = connection.prepareCall("{call sp1()}");
        cstmt.execute();

        List<String> linesAfterExecution = returnLinesFromFile();
        int lineCount = linesAfterExecution.size() - linesBeforeExecution.size();
        assertThat(lineCount, equalTo(1));

        int zeroIndex = linesBeforeExecution.size();
        String expectedQuery = "{call sp1()}";

        String firstQuery = linesAfterExecution.get(zeroIndex);
        assertThat(firstQuery, containsString(expectedQuery));

    }

    public void storedProcedureWithParam() throws SQLException {
        List<String> linesBeforeExecution = returnLinesFromFile();

        CallableStatement cstmt = connection.prepareCall("{call sp2(?,?,?)}");
        cstmt.setString(1, "gokul");
        cstmt.setInt(2, 30);
        cstmt.setDouble(3, 5.01);
        cstmt.execute();

        List<String> linesAfterExecution = returnLinesFromFile();
        int lineCount = linesAfterExecution.size() - linesBeforeExecution.size();
        assertThat(lineCount, equalTo(1));

        int zeroIndex = linesBeforeExecution.size();
        String expectedQuery = "{call sp2('gokul',30,5.01)}";

        String firstQuery = linesAfterExecution.get(zeroIndex);
        assertThat(firstQuery, containsString(expectedQuery));
    }
    
    @Test(expectedExceptions = SQLException.class)
    public void runSQLWithErrors() throws SQLException {
    	 Statement s = connection.createStatement();
    	 s.execute("asdasdas");
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
