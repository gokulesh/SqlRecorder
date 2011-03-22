package org.sqlrecorder.config;

import org.apache.commons.lang.StringUtils;

public final class RuntimeResultConfiguration {

    private static final InheritableThreadLocal<String> CURRENT_FUNCTIONAL_REQUEST = new InheritableThreadLocal<String>() {
        protected String initialValue() {
            return "";
        }
    };

    public static final void setCurrentFunctionalRequestId(String request) {
        if(StringUtils.isNotBlank(request)){
            RuntimeResultConfiguration.CURRENT_FUNCTIONAL_REQUEST.set(request);
        }        
    }

    public static final String getCurrentFunctionalRequest() {
        return CURRENT_FUNCTIONAL_REQUEST.get();
    }

}
