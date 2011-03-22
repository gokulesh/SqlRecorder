package org.sqlrecorder.events.listener;

import com.google.common.io.Files;
import org.sqlrecorder.config.RuntimeResultConfiguration;
import org.sqlrecorder.events.event.ExecuteEvent;
import org.sqlrecorder.events.filter.AllowAllSqlFilter;
import org.sqlrecorder.events.filter.DenyAllSqlFilter;
import org.sqlrecorder.events.filter.SqlOutputFilter;
import org.sqlrecorder.exception.SQLRecorderException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.StringContains.containsString;

@Test
public class FileOutputListenerTest {

    private String fileName = "/tmp/sql.log";
    private String query = "select * from employee";
    private int queryExecutionCount = 2;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullFileNameShouldThrowException(){
        new FileOutputListener(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyFileNameShouldThrowException(){
        new FileOutputListener(" ");
    }

    @Test(expectedExceptions = SQLRecorderException.class)
    public void dirNameShouldThrowException(){
        String fileName = "/tmp";
        new FileOutputListener(fileName);
        File f = new File(fileName);
    }

    public void validFileNameShouldResultInFileBeingCreated(){
        String fileName = "/tmp/sql.log";
        new FileOutputListener(fileName);
        File f = new File(fileName);
        assertThat(fileName + " should have been created",f.exists(),is(true));
        f.delete();
    }

    public void passedInSqlShouldBeWrittenToFile() throws IOException {
        List<String> lines = executeAndReturnLinesInFile(null);
        assertThat("Should have 2 line only",lines.size(),is(2));
        assertThat("query does not match",lines.get(0),containsString(query));
        assertThat("query does not match",lines.get(1),containsString(query));        
    }

    public void passedInSqlWithDenyAllFilterShouldNotBeWrittenToFile() throws IOException {
        List<String> lines = executeAndReturnLinesInFile(new DenyAllSqlFilter());
        assertThat("Should have 0 lines only",lines.size(),is(0));
    }

    //Test of filter order
    public void listenerWithDenyAllAndAllowAllFilterShouldNotBeLogged() throws IOException {
        List<String> lines = executeAndReturnLinesInFile(new DenyAllSqlFilter(),new AllowAllSqlFilter());
        assertThat("Should have 0 lines only",lines.size(),is(0));
        lines = executeAndReturnLinesInFile(new AllowAllSqlFilter(),new DenyAllSqlFilter());
        assertThat("Should have 2 line only",lines.size(),is(0));
    }

    public void passedInSqlWithAllowAllAndDenyAllFilterShouldNotBeWrittenToFile() throws IOException {
    }

    private List<String> executeAndReturnLinesInFile(SqlOutputFilter...filters) throws IOException {
        RuntimeResultConfiguration.setCurrentFunctionalRequestId("testreqid");

        FileOutputListener listener = new FileOutputListener(fileName,filters);
        for(int i=0; i< queryExecutionCount; i++){
            ExecuteEvent event = new ExecuteEvent(query,null);
            listener.queryExecuted(event);
        }
        listener.shutDown();

        File f = new File(fileName);
        List<String> lines = Files.readLines(f, Charset.forName("UTF-8"));
        f.delete();

        return lines;
    }
}
