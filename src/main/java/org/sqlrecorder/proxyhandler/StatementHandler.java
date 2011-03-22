package org.sqlrecorder.proxyhandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.sqlrecorder.events.ActiveListenersManager;
import org.sqlrecorder.events.event.ExecuteEvent;
import org.sqlrecorder.util.LogUtils;

public final class StatementHandler implements InvocationHandler {
    private static final Logger LOG = LogUtils.loggerForThisClass();

    private final Statement statement;
    private final ActiveListenersManager activeListenersManager;

    private List<String> batchSqls = Lists.newArrayList();

    public StatementHandler(Statement statement, ActiveListenersManager activeListenersManager) {
        this.statement = statement;
        this.activeListenersManager = activeListenersManager;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        String methodName = method.getName();

        LOG.debug(String.format("Executed method : %s", methodName));

        ifAddBatch(methodName, args);
        ifSingleQueryExecute(methodName, args);
        ifBatchQueriesExecute(methodName);
        ifClearBatch(methodName);
        ifCloseStatement(methodName);

        return method.invoke(statement, args);
    }

    private void ifSingleQueryExecute(String methodName, Object[] args) {
        if (methodName.startsWith("execute") && !methodName.equals("executeBatch")) {
            String sql = (String) args[0];
            notifyListeners(sql);
        }
    }

    private void ifBatchQueriesExecute(String methodName) {
        if (methodName.equals("executeBatch")) {
            notifyListenersForBatchSql();
        }
    }

    private void ifAddBatch(String methodName, Object[] args) {
        if (methodName.equals("addBatch")) {
            String sql = (String) args[0];
            batchSqls.add(sql);
        }
    }

    private void ifCloseStatement(String methodName) {
        if (methodName.equals("close")) {
            batchSqls.clear();
        }
    }

    private void ifClearBatch(String methodName) {
        if (methodName.equals("clearBatch")) {
            batchSqls.clear();
        }
    }

    private void notifyListenersForBatchSql() {
        for (String batchSql : batchSqls) {
            notifyListeners(batchSql);
        }
    }

    private void notifyListeners(String sql) {
        ExecuteEvent e = new ExecuteEvent(sql);
        activeListenersManager.notifyListeners(e);
    }
}
