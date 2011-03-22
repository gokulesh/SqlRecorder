package org.sqlrecorder.events.filter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Test
public class DenyAllSqlFilterTest {

    private DenyAllSqlFilter sqlFilter;
    @BeforeClass
    public void setUp() {
        sqlFilter = new DenyAllSqlFilter();
    }

    @Test
    public void anySqlShouldbeDenied() throws Exception {
        assertThat(sqlFilter.filter(""),is(true));
        assertThat(sqlFilter.filter(null),is(true));
        assertThat(sqlFilter.filter("select * from table1"),is(true));

    }
}
