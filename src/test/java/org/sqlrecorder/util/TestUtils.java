package org.sqlrecorder.util;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class TestUtils {

    public static  void deRegisterAllDrivers() throws SQLException {
        Enumeration<Driver> allRegDrivers = DriverManager.getDrivers();
        while (allRegDrivers.hasMoreElements()) {
            DriverManager.deregisterDriver(allRegDrivers.nextElement());
        }
    }

}
