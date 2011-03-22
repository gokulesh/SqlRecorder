package org.sqlrecorder.events.listener;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class QueryOutputCollectorListener extends BaseStatementListener {

    private static final String EXECUTED_QUERIES = "EXECUTED_QUERIES";
    private static final String LISTENER_ID = "query-collector";

    private final ThreadLocal<Map<String,ThreadLocalQueriesResult >> results;

    public QueryOutputCollectorListener() {
        results = new ThreadLocal<Map<String,ThreadLocalQueriesResult>>() {
            protected Map<String,ThreadLocalQueriesResult> initialValue() {
                Map<String,ThreadLocalQueriesResult> map =  Maps.newHashMap();
                map.put(EXECUTED_QUERIES,new ThreadLocalQueriesResult());
                return map;
            }
        };
    }

    @Override
    public String id() {
        return LISTENER_ID;
    }

    @Override
    protected void processEvent(String sql) {
        Map<String,ThreadLocalQueriesResult > resultMap = results.get();
        resultMap.get(EXECUTED_QUERIES).addQuery(sql);
    }

    public boolean returnsExecutedQueries() {
        return true;
    }

    public void shutDown() {}

    public List<String> executedQueries() {
        return results.get().get(EXECUTED_QUERIES).executedQueries();
    }
}
