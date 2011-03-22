package org.sqlrecorder.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.testng.annotations.Test;

@Test
public class JDBCUtilsTest {

    public void assertCorrectMethodsPickedUpFromInterfaceForPreparedStatements() {
        List<Method> methods = JDBCUtils.createListOfMonitorablePreparedStatementMethodNames();
        assertThat(methods.size(), equalTo(48));

        Map<String, Method> map = Maps.newHashMap();
        for (Method m : methods) {
            map.put(m.getName(), m);
        }

        assertThat(map.get("setBoolean"),is(notNullValue()));
        assertThat(map.get("setInt"),is(notNullValue()));
        assertThat(map.get("setString"),is(notNullValue()));
        assertThat(map.get("setFloat"),is(notNullValue()));
        assertThat(map.get("setShort"),is(notNullValue()));
        assertThat(map.get("setLong"),is(notNullValue()));
        assertThat(map.get("setObject"),is(notNullValue()));
        assertThat(map.get("setDate"),is(notNullValue()));
        assertThat(map.get("setTimestamp"),is(notNullValue()));

    }
}

