package org.sqlrecorder.events.listener;

import java.util.List;

import org.sqlrecorder.events.event.ExecuteEvent;

public interface StatementListener {
    String id();
    void queryExecuted(ExecuteEvent e);
    List<String> executedQueries();
    boolean returnsExecutedQueries();
    void shutDown();
}
