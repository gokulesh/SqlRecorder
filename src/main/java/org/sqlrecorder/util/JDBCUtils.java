package org.sqlrecorder.util;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class JDBCUtils {

    public static List<Method> createListOfMonitorablePreparedStatementMethodNames() {

        List<Method> monitorableSetMethodNames = new ArrayList<Method>(48);
        Method[] methods = PreparedStatement.class.getMethods();

        for (Method m : methods) {
            if (!m.getName().startsWith("set")) {
                continue;
            }

            Class<?>[] paramTypes = m.getParameterTypes();
            if (ArrayUtils.isEmpty(paramTypes)) {
                continue;
            }
            //Should not be a normal setter(). Must be a setter of parameters
            // which has a position index(Integer class) as the first argument  
            if (paramTypes.length >= 2 && paramTypes[0] == int.class) {
                monitorableSetMethodNames.add(m);
            }
        }
        return monitorableSetMethodNames;
    }
}
