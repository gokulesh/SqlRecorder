package org.sqlrecorder.events.event;

public class ParameterContext {

    private final String value;
    private final Class type;
    private final Integer position;

    public ParameterContext(final Integer position,final String paramValue, final Class type ) {
        this.value = paramValue;
        this.type = type;
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public Class getType() {
        return type;
    }

    public Integer getPosition() {
        return position;
    }
}
