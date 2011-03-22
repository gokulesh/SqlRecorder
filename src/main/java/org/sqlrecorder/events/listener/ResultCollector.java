package org.sqlrecorder.events.listener;

import java.util.List;

public interface ResultCollector {
    List<String> executedQueries();
}
