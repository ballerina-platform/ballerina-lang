package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.List;

public class Option {
    private final String key;
    private String value;
    private final TemplateBuilder templateBuilder;
    private List<String> variableValues = new ArrayList<>();

    public Option(String key, String value, TemplateBuilder templateBuilder) {
        this.key = key;
        this.value = value;
        this.templateBuilder = templateBuilder;
    }

    public void resetValue(){
        value = null;
    }

    public int addVariableValue(String value){
        variableValues.add(value);
        return (variableValues.size() - 1);
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
        if (dynamicOptions.getVariableOptionIndex() != -1) {
            return variableValues.get(dynamicOptions.getVariableOptionIndex());
        } else if (value != null) {
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
