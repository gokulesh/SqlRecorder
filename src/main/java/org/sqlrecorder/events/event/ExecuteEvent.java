package org.sqlrecorder.events.event;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public final class ExecuteEvent {

    private final String sql;
    private final List<ParameterContext> queryParams;

    public ExecuteEvent(final String sql, final List<ParameterContext> queryParams) {
        Preconditions.checkArgument(StringUtils.isNotBlank(sql), "Sql cannot be empty");
        this.sql = sql;
        this.queryParams = (queryParams != null) ? Collections.unmodifiableList(Lists.newArrayList(queryParams)) : null;
    }

    public ExecuteEvent(final String sql) {
        this(sql, null);
    }

    //TODO move this to a listener ?
    public String getFinalSql() {

        //This will be the case for normal sql
        if (CollectionUtils.isEmpty(queryParams)) {
            return sql;
        }

        StringBuilder sb = new StringBuilder(sql);
        int questionMarkPosition = -1;
        int currentPosition = 0;
        short paramCount = 0;
        boolean createSqlWithParamsSeparately = false;
        while ((questionMarkPosition = sb.indexOf("?", currentPosition)) != -1) {

            if(paramCount >= queryParams.size()){
                //Possibly a stored procedure that has both in and out param registered in which case this will not work.
                createSqlWithParamsSeparately = true ;
                break;                
            }
            ParameterContext context = queryParams.get(paramCount++);
            if(context == null){
                //Possibly a stored procedure that has both in and out param registered in which case this will not work.
                createSqlWithParamsSeparately = true ;
                break;
            }
            String paramValue = context.getValue();
            if (context.getType().equals(String.class)) {
                paramValue = "'" + paramValue + "'";
            }
            sb = sb.replace(questionMarkPosition, questionMarkPosition + 1, paramValue);
            currentPosition = questionMarkPosition + paramValue.length();
        }

        if(!createSqlWithParamsSeparately){
            return sb.toString(); 
        }
        //TODO refactor this code to smaller methods and remove the duplicate construction
        sb = new StringBuilder(sql);
        sb. append(" set param. :[");
        paramCount = 1;
        for(ParameterContext context : queryParams){
            if(paramCount++ > 1){
                sb.append(",");
            }
            if(context == null){
                sb.append("?");
                continue;
            }
            String paramValue = context.getValue();
            if (context.getType().equals(String.class)) {
                sb.append("'").append(paramValue).append("'");
            }else{
                sb.append(paramValue);
            }
        }
        sb. append("]");
        return sb.toString();
    }
}
