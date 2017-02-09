package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;

public class OptionHolder {
    private final Map<String, String> options;
    private final Map<String, String> dynamicOptions;
    private final HashMap<String, TemplateBuilder> dynamicOptionConverters;

    public OptionHolder(StreamDefinition streamDefinition, Map<String, String> options, Map<String, String> dynamicOptions) {
        this.options = options;
        this.dynamicOptions = dynamicOptions;
        dynamicOptionConverters = new HashMap<String, TemplateBuilder>();
        for (Map.Entry<String, String> entry : dynamicOptions.entrySet()) {
            dynamicOptionConverters.put(entry.getKey(),
                    new TemplateBuilder(streamDefinition, entry.getValue()));
        }
    }

    public String getOption(String optionKey, Event event) {
        String optionValue = options.get(optionKey);
        if (optionValue == null) {
            TemplateBuilder templateBuilder = dynamicOptionConverters.get(optionKey);
            if (templateBuilder != null) {
                return templateBuilder.build(event);
            } else {
                return null;
            }
        } else {
            return optionValue;
        }
    }

    public String getStaticOption(String optionKey) {
        return options.get(optionKey);
    }

    public String getDynamicOption(String optionKey, Event event) {
        TemplateBuilder templateBuilder = dynamicOptionConverters.get(optionKey);
        if (templateBuilder != null) {
            return templateBuilder.build(event);
        }
        return null;
    }

    public boolean containsOption(String optionKey) {
        return options.containsKey(optionKey) || dynamicOptions.containsKey(optionKey);
    }
}
