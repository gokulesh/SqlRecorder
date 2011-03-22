package org.sqlrecorder.events.filter;

public class DenyAllSqlFilter implements SqlOutputFilter {
    public String id() {
        return "DenyAllSqlFilter";
    }

    public boolean filter(String sql) {
        return true;
    }
}
