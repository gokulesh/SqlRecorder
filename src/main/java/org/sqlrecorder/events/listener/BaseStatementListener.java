package org.sqlrecorder.events.listener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sqlrecorder.events.event.ExecuteEvent;
import org.sqlrecorder.events.filter.AllowAllSqlFilter;
import org.sqlrecorder.events.filter.SqlOutputFilter;
import org.sqlrecorder.util.LogUtils;

import java.util.Arrays;
import java.util.List;

public abstract class BaseStatementListener implements StatementListener {

    private static final Logger LOG = LogUtils.loggerForThisClass();

    private final List<SqlOutputFilter> sqlOutputFilters = Lists.newArrayList();

    public BaseStatementListener (){
        this(new AllowAllSqlFilter());
    }

    public BaseStatementListener(SqlOutputFilter... sqlOutputFilters) {
        if (!ArrayUtils.isEmpty(sqlOutputFilters)) {
            this.sqlOutputFilters.addAll(Arrays.asList(sqlOutputFilters));
            logFiltersAdded();
        }
    }

    public abstract String id();
    protected abstract void processEvent(String sql);

    public void queryExecuted(final ExecuteEvent e) {
        if (excludedByAnyFilter(e.getFinalSql())){
            return;
        }
        processEvent(e.getFinalSql());
    }

    @Override
    public String toString() {
        return id();
    }

    private boolean excludedByAnyFilter(String sql) {
        for (SqlOutputFilter outputFilter : sqlOutputFilters) {
            if (outputFilter.filter(sql)) {
                return true;
            }
        }
        return false;
    }

    private void logFiltersAdded() {
        for (SqlOutputFilter filter : sqlOutputFilters) {
            LOG.info(String.format("Adding filter: %s", filter.id()));
        }
    }
}
