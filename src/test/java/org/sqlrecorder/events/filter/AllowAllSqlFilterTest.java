package org.sqlrecorder.events.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AllowAllSqlFilterTest {

    private AllowAllSqlFilter sqlFilter;

    @BeforeMethod
    public void setup(){
        sqlFilter = new AllowAllSqlFilter();
    }

    public void anySqlShouldBeAllowed(){
        assertThat(sqlFilter.filter(""),is(false));
        assertThat(sqlFilter.filter(null),is(false));
        assertThat(sqlFilter.filter("select * from table1"),is(false));
    }
}
