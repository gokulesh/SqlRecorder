package org.sqlrecorder.proxyhandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.sqlrecorder.events.ActiveListenersManager;
import org.sqlrecorder.events.event.ExecuteEvent;
import org.sqlrecorder.events.event.ParameterContext;
import org.sqlrecorder.exception.SQLRecorderException;
import org.sqlrecorder.util.JDBCUtils;
import org.sqlrecorder.util.LogUtils;

public final class PreparedStatementHandler implements InvocationHandler {
    private static final Logger LOG = LogUtils.loggerForThisClass();

    private final PreparedStatement preparedStatement;
    private final String preparedStatementSql;
    private final ActiveListenersManager activeListenersManager;

    private final List<ParameterContext> params;

    //The total no. of valid methods available for being set with a param index
    private final List<Method> monitorableSetMethodNames;
    private List<ExecuteEvent> batchSqlEvents = Lists.newArrayList();

    public PreparedStatementHandler(PreparedStatement statement, String preparedStatementSql, ActiveListenersManager activeListenersManager) {
        this.preparedStatement = statement;
        this.preparedStatementSql = preparedStatementSql;
        this.activeListenersManager = activeListenersManager;
        this.monitorableSetMethodNames = JDBCUtils.createListOfMonitorablePreparedStatementMethodNames();

        try {
            int paramCount = statement.getParameterMetaData().getParameterCount();
            params = new ArrayList<ParameterContext>(paramCount);
            for(int i=0; i<paramCount; i++){
                params.add(null);//Initialize with null so that the positions can be set correctly when adding parameters
            }
        } catch (SQLException e) {
            throw new SQLRecorderException("Unable to get parameter count for prepared statement: " + preparedStatementSql);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        LOG.debug(String.format("Executed method in PreparedStatementHandler : %s", methodName));

        ifAnyOfSetMethods(method, args);
        ifExecuteQuery(methodName);
        ifClearParameters(methodName);
        ifAddBatch(methodName);
        ifExecuteBatch(methodName);
        ifClearBatch(methodName);

        return method.invoke(preparedStatement, args);
    }

    private void ifAnyOfSetMethods(Method method, Object[] args) {
        if (monitorableSetMethodNames.contains(method)) {
            //As the prepared statement paramPosition start from 1 and we use 0 based paramPosition for array, subtract 1 from paramPosition position.
            Integer paramPosition = (Integer) args[0];
            String paramValue = args[1].toString();

            params.set(paramPosition - 1, new ParameterContext(paramPosition, paramValue, args[1].getClass()));
            LOG.debug(String.format("Added parameters for prepared statement: %d - %s", paramPosition, paramValue.toString()));
        }
    }

    private void ifExecuteQuery(String methodName) {
        if (methodName.startsWith("execute") && !methodName.equals("executeBatch")) {
            ExecuteEvent e = new ExecuteEvent(preparedStatementSql, params);
            activeListenersManager.notifyListeners(e);
        }
    }

    private void ifAddBatch(String methodName) {
        if (methodName.startsWith("addBatch")) {
            ExecuteEvent e = new ExecuteEvent(preparedStatementSql, params);
            batchSqlEvents.add(e);
        }
    }

    private void ifExecuteBatch(String methodName) {
        if (!methodName.equals("executeBatch")) {
            return;
        }
        for (ExecuteEvent e : batchSqlEvents) {
            activeListenersManager.notifyListeners(e);
        }
    }

    private void ifClearBatch(String methodName) {
        if (methodName.equals("clearBatch")) {
            batchSqlEvents.clear();
        }
    }

    private void ifClearParameters(String methodName) {
        if (methodName.equals("clearParameters")) {
            params.clear();
        }
    }
}