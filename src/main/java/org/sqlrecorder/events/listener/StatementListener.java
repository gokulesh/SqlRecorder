package org.sqlrecorder.events.listener;

import java.util.List;

import org.sqlrecorder.events.event.ExecuteEvent;

public interface StatementListener {

    /**
     * @return string identifying this listener
     */
    String id();

    /**
     * @param e The query that was just executed is encapsulated as an ExecuteEvent. Retrieve
     *          the details and apply filters if necessary to log it.
     *
     *          Listeners can be configured with as many filters as it needs at startup time. Filters
     *          will be applied in this method.
     */
    void queryExecuted(ExecuteEvent e);

    /**
     * Perform any cleanup class when the app is shutting down listeners(like closing files etc..)
     */
    void shutDown();

    /**
     * Experimental features
     * @return empty list for now
     */
    List<String> executedQueries();

    /**
     * Experimental features
     * @return false for now
     */
    boolean returnsExecutedQueries();
}
