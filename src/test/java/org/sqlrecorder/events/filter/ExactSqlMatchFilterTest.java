package org.sqlrecorder.events.filter;

import org.sqlrecorder.exception.SQLFilterException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Test
public class ExactSqlMatchFilterTest {

    private ExactSqlMatchFilter sqlFilter;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void filterWithNoSqlShouldThrowException(){
        sqlFilter = new ExactSqlMatchFilter();
    }

    @Test(expectedExceptions = SQLFilterException.class)
    public void filterWithEmptySqlShouldThrowException(){
        sqlFilter = new ExactSqlMatchFilter("Select 1","   ");
    }

    @Test(expectedExceptions = SQLFilterException.class)
    public void filterWithNullSqlShouldThrowException(){
        sqlFilter = new ExactSqlMatchFilter("Select 1",null);
    }

    public void filterWithSqlNotInListShouldReturnFalse(){
        sqlFilter = new ExactSqlMatchFilter("Select 1");
        assertThat(sqlFilter.filter("Select * from table1"),is(false));
    }

    public void filterWithSqlInListShouldReturnTrue(){
        sqlFilter = new ExactSqlMatchFilter("Select 1");
        assertThat(sqlFilter.filter("Select 1"),is(true));
    }
}
