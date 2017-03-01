package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;

public class Option {
    private final String key;
    private final String value;
    private final Type type;
    private final TemplateBuilder templateBuilder;

    public Option(String key, String value, Type type, TemplateBuilder templateBuilder) {

        this.key = key;
        this.value = value;
        this.type = type;
        this.templateBuilder = templateBuilder;
    }

    public enum Type {
        DYNAMIC, STATIC
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public String getValue(Event event) {
        if (value != null) {
            return value;
        } else if (templateBuilder != null) {
            return templateBuilder.build(event);
        } else {
            return null;
        }
    }

    public String getValue(ComplexEvent complexEvent) {
        if (value != null) {
            return value;
        } else if (templateBuilder != null) {
            return templateBuilder.build(complexEvent);
        } else {
            return null;
        }
    }
}
