package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.core.event.Event;

public class Option {
    private final String key;
    private final String value;
    private final TemplateBuilder templateBuilder;

    public Option(String key, String value, TemplateBuilder templateBuilder) {
        this.key = key;
        this.value = value;
        this.templateBuilder = templateBuilder;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isStatic() {
        return templateBuilder ==null;
    }

    public String getValue(DynamicOptions dynamicOptions) {
        if (value != null) {
            return value;
        } else if (templateBuilder != null) {
            return templateBuilder.build(dynamicOptions.getEvent());
        } else {
            return null;
        }
    }

    //TODO: Add documentation
     public String getValue(Event event) {
        if (value != null) {
            return value;
        } else if (templateBuilder != null) {
            return templateBuilder.build(event);
        } else {
            return null;
        }
    }

}
