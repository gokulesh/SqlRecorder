package org.sqlrecorder;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sqlrecorder.config.RuntimeResultConfiguration;
import org.sqlrecorder.events.ActiveListenersManager;
import org.sqlrecorder.events.listener.StatementListener;
import org.sqlrecorder.exception.SQLRecorderException;
import org.sqlrecorder.proxyhandler.ConnectionHandler;
import org.sqlrecorder.util.LogUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class SqlRecorder implements Driver {

    private static final Logger LOG = LogUtils.loggerForThisClass();

    private final ActiveListenersManager manager;
    private final String driverClass;
    private Driver driver;

 
    public SqlRecorder(String driverClass, List<StatementListener> listenerList) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(driverClass), "driver class cannot be empty");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(listenerList), "Must register at least 1 listener");

        LOG.info(String.format("Starting to register drivers: %s", driverClass));
        LOG.info(String.format("Starting to register listeners: %s", listenerList));

        this.driverClass = driverClass;
        this.manager = new ActiveListenersManager(listenerList.toArray(new StatementListener[]{}));

        proxyDriver();
        LOG.info("Finished startup..");
    }

    public List<String> executedQueries() {
        return manager.getExecutedQueries();
    }

    public void setCurrentFunctionalRequestId(String requestId) {
        RuntimeResultConfiguration.setCurrentFunctionalRequestId(requestId);
    }

    public Connection connect(String s, Properties properties) throws SQLException {
        Connection connection = driver.connect(s, properties);
        ConnectionHandler connHandler = new ConnectionHandler(connection, manager);
        Connection proxyConnection = (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Connection.class}, connHandler);
        LOG.debug(String.format("Created a new proxy connection for native connection : %s", connection.toString()));
        return proxyConnection;
    }

    public boolean acceptsURL(String s) throws SQLException {
        return driver.acceptsURL(s);
    }

    public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
        return driver.getPropertyInfo(s, properties);
    }

    public int getMajorVersion() {
        return driver.getMajorVersion();
    }

    public int getMinorVersion() {
        return driver.getMinorVersion();
    }

    public boolean jdbcCompliant() {
        return driver.jdbcCompliant();
    }

    private synchronized void proxyDriver() {
        try {

            //Load the driver we want to proxy
            Class.forName(driverClass);

            LOG.info("Starting to identify currently registered jdbc drivers..");
            Enumeration<Driver> allRegDrivers = DriverManager.getDrivers();

            //Now enumerate thro the registered drivers  and deregister all the drivers so that SqlRecorder becomes the first driver in queue.
            //Add the other drivers to the end of the queue
            LOG.info(String.format("Processing user input driver: %s", driverClass));
            Driver driver = null;
            List<Driver> unregisteredDrivers = Lists.newArrayList();
            while (allRegDrivers.hasMoreElements()) {
                driver = allRegDrivers.nextElement();
                LOG.info(String.format("Found registered jdbc driver: %s ", driver.getClass().getName()));
                if (driver.getClass().getName().equals(driverClass)) {
                    this.driver = driver;
                    LOG.info(String.format("Matched existing driver: registered driver: %s, user input: %s. Deregistering driver", driver.getClass().getName(), driverClass));
                }
                DriverManager.deregisterDriver(driver);
                unregisteredDrivers.add(driver);//Keep track of unreg drivers
            }

            //This case would occur if the driver has been deregistered and not registered again till this point. 
            if(this.driver == null){
                this.driver = (Driver)Class.forName(driverClass).newInstance();
            }
            DriverManager.registerDriver(this);

            //Now reregister the unregistered drivers. They will come after this proxy driver.
            //The driver we are proxying will not be registered
            for (Driver unregisteredDriver : unregisteredDrivers) {
                if (!unregisteredDriver.getClass().getName().equals(driverClass)) {
                    DriverManager.registerDriver(unregisteredDriver);
                }
            }
        } catch (Exception e) {
            throw new SQLRecorderException("Unexpected error when registering drivers", e);
        }
    }
}
