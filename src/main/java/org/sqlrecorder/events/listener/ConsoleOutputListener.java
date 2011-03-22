package org.sqlrecorder.events.listener;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sqlrecorder.config.RuntimeResultConfiguration;
import org.sqlrecorder.events.filter.SqlOutputFilter;
import org.sqlrecorder.exception.SQLRecorderException;

public final class ConsoleOutputListener extends BaseStatementListener {

    public String id() {
        return "console-logger";
    }

    public ConsoleOutputListener() {
        super();
    }

    public ConsoleOutputListener(SqlOutputFilter... sqlOutputFilters) {
        super(sqlOutputFilters);
    }

    public List<String> executedQueries() {
        throw new SQLRecorderException("Does not return the list of queries executed");
    }

    public boolean returnsExecutedQueries() {
        return false;
    }

    public void shutDown() {
    }

    @Override
    protected void processEvent(String sql) {
        String requestId = RuntimeResultConfiguration.getCurrentFunctionalRequest();
        if (StringUtils.isNotBlank(requestId)) {
            requestId += ":";
        }
        System.out.print(requestId + sql + "\n");
        System.out.flush();
    }

}
