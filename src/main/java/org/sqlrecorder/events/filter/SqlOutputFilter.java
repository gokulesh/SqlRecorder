package org.sqlrecorder.events.filter;

public interface SqlOutputFilter {
    String id();

    //if it returns true, the sql will not be logged to o/p(console,file etc..)
    boolean filter(String sql);
}

