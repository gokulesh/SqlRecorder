package org.sqlrecorder.events.filter;

public class AllowAllSqlFilter implements SqlOutputFilter {
    public String id() {
        return "AllowAllSqlFilter";
    }

    public boolean filter(String sql) {
        return false;
    }
}
