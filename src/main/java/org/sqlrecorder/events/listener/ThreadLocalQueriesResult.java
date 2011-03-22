package org.sqlrecorder.events.listener;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

public final class ThreadLocalQueriesResult {

    private List<String> queries = Lists.newArrayList();

    public List<String> executedQueries() {
        return queries;
    }

    public void addQuery(String query) {
        Preconditions.checkArgument(StringUtils.isNotBlank(query),"Empty query cannot be inserted in result");
        queries.add(query);
    }

    public void clear(){
        queries.clear();
    }
}