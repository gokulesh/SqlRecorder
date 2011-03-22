package org.sqlrecorder.events.filter;

public interface SqlOutputFilter {

    /**
     *
     * @return An string that identifies this filter
     */
    String id();

    /**
     * @param sql the sql to filter
     * @return true , if the the sql will not be logged to o/p(console,file etc..) false otherwise
     */
    boolean filter(String sql);
}

