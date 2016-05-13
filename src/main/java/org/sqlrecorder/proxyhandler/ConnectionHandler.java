package org.sqlrecorder.proxyhandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.sqlrecorder.events.ActiveListenersManager;
import org.sqlrecorder.util.LogUtils;

public final class ConnectionHandler implements InvocationHandler {

    private static final Logger LOG = LogUtils.loggerForThisClass();

    private final Connection connection;
    private final ActiveListenersManager activeListenersManager;

    public ConnectionHandler(Connection connection, ActiveListenersManager activeListenersManager) {
        this.connection = connection;
        this.activeListenersManager = activeListenersManager;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws SQLException {
        Object o;
		try {
			o = method.invoke(connection, args);
		} catch (IllegalArgumentException e) {
			throw new SQLException(e.getCause());
		} catch (IllegalAccessException e) {
			throw new SQLException(e.getCause());
		} catch (InvocationTargetException e) {
			throw new SQLException(e.getCause());
		}
        String methodName = method.getName();

        LOG.debug(String.format("Executed method in ConnectionHandler : %s",methodName));
        if (isStatement(methodName)) {
            return checkAndCreateStatement(o);
        } else if (isPreparedStatement(methodName)) {
            return checkAndCreatePreparedStatement(args, o);
        }else if(isCallableStatement(methodName)){
            return checkAndCreateCallableStatement(args, o);
        }
        return o;
    }

    private Object checkAndCreateCallableStatement(Object[] args, Object o) {
        CallableStatement callableStmt = (CallableStatement) o;
        PreparedStatementHandler pStmtHandler = new PreparedStatementHandler(callableStmt, (String) args[0], activeListenersManager);
        CallableStatement proxyStmt = (CallableStatement) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{CallableStatement.class}, pStmtHandler);
        LOG.debug("Created new proxy callable statement");
        return proxyStmt;
    }

    private Object checkAndCreatePreparedStatement(Object[] args, Object o) {
        PreparedStatement pStmt = (PreparedStatement) o;
        PreparedStatementHandler pStmtHandler = new PreparedStatementHandler(pStmt, (String) args[0], activeListenersManager);
        PreparedStatement proxyStmt = (PreparedStatement) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{PreparedStatement.class}, pStmtHandler);
        LOG.debug("Created new proxy prepared statement");
        return proxyStmt;
    }

    private Object checkAndCreateStatement(Object o) {
        Statement stmt = (Statement) o;
        StatementHandler stmtHandler = new StatementHandler(stmt, activeListenersManager);
        Statement proxyStmt = (Statement) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Statement.class}, stmtHandler);
        LOG.debug("Created new proxy statement");
        return proxyStmt;
    }

    private boolean isCallableStatement(String methodName) {
        return methodName.startsWith("prepareCall");
    }

    private boolean isPreparedStatement(String methodName) {
        return methodName.startsWith("prepareStatement");
    }

    private boolean isStatement(String methodName) {
        return methodName.startsWith("createStatement");
    }
}
