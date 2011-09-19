package org.sqlrecorder.events.filter;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.sqlrecorder.exception.SQLFilterException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ContainsSQLFilter implements SqlOutputFilter {
	private List<String> sqlToFilterList;

    public ContainsSQLFilter(String... sqlToFilterList){
        Preconditions.checkArgument(!ArrayUtils.isEmpty(sqlToFilterList),"Must have at least 1 predefined sql to filter");
        for(String sql : sqlToFilterList){
            if(StringUtils.isBlank(sql)){
                throw new SQLFilterException("cannot have empty sql statements to filter");
            }
        }
        this.sqlToFilterList = Lists.newArrayList(sqlToFilterList);
    }

    public String id() {
        return "ExactSqlMatchFilter";
    }

    public boolean filter(String sqlToFilter) {
        for(String predefinedSql : sqlToFilterList){
            if((sqlToFilter.contains(predefinedSql))) {
                return true;
            }
        }
        return false;
    }
}
