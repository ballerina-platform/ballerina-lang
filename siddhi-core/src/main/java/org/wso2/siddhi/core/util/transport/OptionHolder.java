package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.HashMap;
import java.util.Map;

public class OptionHolder {

    private final Map<String, Option> options = new HashMap<>();
    private final Extension extension;

    public OptionHolder(StreamDefinition streamDefinition, Map<String, String> staticOptions,
                        Map<String, String> dynamicOptions, Extension extension) {
        this.extension = extension;
        for (Map.Entry<String, String> entry : staticOptions.entrySet()) {
            options.put(entry.getKey(), new Option(entry.getKey(), entry.getValue(), null));
        }
        for (Map.Entry<String, String> entry : dynamicOptions.entrySet()) {
            options.put(entry.getKey(), new Option(entry.getKey(), null,
                    new TemplateBuilder(streamDefinition, entry.getValue())));
        }
    }

    public Option validateAndGetOption(String optionKey) {
        Option option = options.get(optionKey);
        if (option == null) {
            throw new ExecutionPlanValidationException("Option '" + optionKey + "' does not exist in the configuration" +
                    " of '" + extension.namespace() + ":" + extension.name() + "'.");
        }
        return option;
    }

    public Option getOrCreateOption(String optionKey, String defaultValue) {
        Option option = options.get(optionKey);
        if (option == null) {
            option = new Option(optionKey, defaultValue, null);
        }
        return option;
    }

    public String validateAndGetStaticValue(String optionKey, String defaultValue) {
        Option option = options.get(optionKey);
        if (option != null) {
            if (!option.isStatic()) {
                throw new ExecutionPlanValidationException("'" + optionKey + "' is not a 'static' " +
                        "option in the configuration of " + extension.namespace() + ":" + extension.name() + ".");
            }
            return option.getValue();
        } else {
            return defaultValue;
        }
    }

    public String validateAndGetStaticValue(String optionKey) {
        Option option = options.get(optionKey);
        if (option != null) {
            if (!option.isStatic()) {
                throw new ExecutionPlanValidationException("'" + optionKey + "' is defined as a 'dynamic' option " +
                        "but it has to be a 'static' option for the " + extension.namespace() + ":" +
                        extension.name() + " configuration.");
            }
            return option.getValue();
        } else {
            throw new ExecutionPlanValidationException("'" + optionKey + "' 'static' option is not " +
                    "defined in the configuration of " + extension.namespace() + ":" + extension.name() + ".");
        }
    }

    public boolean isOptionExists(String optionKey){
        return (options.get(optionKey) != null);
    }

//    public void validate(String key, boolean optional) {
//        Option option = options.get(key);
//        if (option == null) {
//            if (!optional) {
//                throw new ExecutionPlanValidationException("'" + key + "' is not provided in the configuration.");
//            }
//        }
//    }

//    public String getOptionValue(String optionKey, Event event) {
//        Option option = options.get(optionKey);
//        if (option != null) {
//            return option.getValue(event);
//        } else {
//            return null;
//        }
//    }

}
