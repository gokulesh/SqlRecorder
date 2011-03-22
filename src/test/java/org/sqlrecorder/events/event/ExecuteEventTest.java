package org.sqlrecorder.events.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.List;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

@Test
public class ExecuteEventTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullSqlShouldThrowException() {
        new ExecuteEvent(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptySqlShouldThrowException() {
        new ExecuteEvent("   ");
    }

    public void statementWithoutParamsShouldReturnSqlAsIs() {
        String query = "select * from table1";
        String finalSql = new ExecuteEvent(query).getFinalSql();
        assertThat(finalSql, is(query));
    }

    public void statementWithParamsShouldReturnSqlWithParamsSubstituted() {
        String initialQuery = "select * from employee where name = ? and postcode=? and height > ?";
        ParameterContext ctx1 = new ParameterContext(1, "gokul", String.class);
        ParameterContext ctx2 = new ParameterContext(2, "3000", Integer.class);
        ParameterContext ctx3 = new ParameterContext(3, "5.10", Float.class);

        List<ParameterContext> ctxs = Lists.newArrayList(ctx1, ctx2, ctx3);
        String actualFinalQuery = new ExecuteEvent(initialQuery, ctxs).getFinalSql();
        String expectedFinalQuery = "select * from employee where name = 'gokul' and postcode=3000 and height > 5.10";
        assertThat(actualFinalQuery, is(expectedFinalQuery));
    }

    public void storedProcedureWithMatchingParamCountShouldReturnSqlWithParamsSubstituted() {
        String initialQuery = "{call sp1(?,?,?)}";
        ParameterContext ctx1 = new ParameterContext(1, "gokul", String.class);
        ParameterContext ctx2 = new ParameterContext(2, "3000", Integer.class);
        ParameterContext ctx3 = new ParameterContext(3, "5.10", Float.class);

        List<ParameterContext> ctxs = Lists.newArrayList(ctx1, ctx2, ctx3);
        String actualFinalQuery = new ExecuteEvent(initialQuery, ctxs).getFinalSql();
        String expectedFinalQuery = "{call sp1('gokul',3000,5.10)}";
        assertThat(actualFinalQuery, is(expectedFinalQuery));
    }

    public void storedProcedureWithInAndOutParamRegisteredShouldNotReturnSqlWithParamsSubstituted() {
        //Here there are 5 parameters, 2 of them are out params.
        String initialQuery = "{? = call sp1(?,?,?,?)}";
        ParameterContext ctx1 = new ParameterContext(1, "gokul", String.class);
        ParameterContext ctx2 = new ParameterContext(2, "3000", Integer.class);
        ParameterContext ctx3 = new ParameterContext(3, "5.10", Float.class);

        List<ParameterContext> ctxs = Lists.newArrayList(ctx1, ctx2, ctx3);
        String actualFinalQuery = new ExecuteEvent(initialQuery, ctxs).getFinalSql();
        String expectedFinalQuery = "{? = call sp1(?,?,?,?)} set param. :['gokul',3000,5.10]";
        assertThat(actualFinalQuery, is(expectedFinalQuery));
    }

    public void anotherStoredProcedureWithInAndOutParamRegisteredShouldNotReturnSqlWithParamsSubstituted() {
        String initialQuery = "{?=call sp1(?,?)}";
        ParameterContext ctx1 = new ParameterContext(1, "gokul", String.class);
        ParameterContext ctx2 = new ParameterContext(2, "3000", Integer.class);

        List<ParameterContext> ctxs = Lists.newArrayList(null,ctx1, ctx2);
        String actualFinalQuery = new ExecuteEvent(initialQuery, ctxs).getFinalSql();
        String expectedFinalQuery = "{?=call sp1(?,?)} set param. :[?,'gokul',3000]";
        assertThat(actualFinalQuery, is(expectedFinalQuery));
    }


}

