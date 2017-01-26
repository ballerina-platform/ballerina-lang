/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.output.mapper.text;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.publisher.OutputMapper;
import org.wso2.siddhi.annotation.SiddhiExtension;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

@SiddhiExtension(
        name = "text",
        namespace = "outputmapper"
)public class TextOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    private static final String EVENT_ATTRIBUTE_SEPARATOR = ",";
    private static final String EVENT_ATTRIBUTE_VALUE_SEPARATOR = ":";

    /**
     * Initialize the mapper and the mapping configurations
     *
     * @param streamDefinition       The stream definition
     * @param options                Additional mapping options
     * @param unmappedDynamicOptions Unmapped dynamic options
     */
    @Override
    public void init(StreamDefinition streamDefinition, Map<String, String> options, Map<String, String> unmappedDynamicOptions) {
        this.streamDefinition = streamDefinition;
    }

    /**
     * Convert the given {@link Event} to TEXT string
     *
     * @param event          Event object
     * @param dynamicOptions Dynamic options per event
     * @return the constructed TEXT string
     */
    @Override
    public Object convertToTypedInputEvent(Event event, Map<String, String> dynamicOptions) {
        StringBuilder eventText = new StringBuilder();
        Object[] data = event.getData();
        for (int i = 0; i < data.length; i++) {
            String attributeName = streamDefinition.getAttributeNameArray()[i];
            Object attributeValue = data[i];
            eventText.append(attributeName).append(EVENT_ATTRIBUTE_VALUE_SEPARATOR).append(attributeValue.toString()).append(EVENT_ATTRIBUTE_SEPARATOR).append("\n");
        }
        eventText.deleteCharAt(eventText.lastIndexOf(EVENT_ATTRIBUTE_SEPARATOR));
        eventText.deleteCharAt(eventText.lastIndexOf("\n"));

        // Get arbitrary data from event
        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
            // Add arbitrary data map to the default template
            eventText.append(EVENT_ATTRIBUTE_SEPARATOR);
            for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                eventText.append("\n" + entry.getKey() + EVENT_ATTRIBUTE_SEPARATOR + entry.getValue() + EVENT_ATTRIBUTE_SEPARATOR);
            }
            eventText.deleteCharAt(eventText.lastIndexOf(EVENT_ATTRIBUTE_SEPARATOR));
            eventText.deleteCharAt(eventText.lastIndexOf("\n"));
        }

        return eventText.toString();
    }

    /**
     * Convert the given Event mapping to TEXT string
     *
     * @param event            Event object
     * @param mappedAttributes Event mapping string array
     * @param dynamicOptions   Dynamic options per event
     * @return the mapped TEXT string
     */
    @Override
    public Object convertToMappedInputEvent(Event event, String[] mappedAttributes, Map<String, String> dynamicOptions) {
        StringBuilder eventText = new StringBuilder();
        for (int i = 0; i < mappedAttributes.length; i++) {
            String mappedAttribute = mappedAttributes[i];
            eventText.append(mappedAttribute).append(EVENT_ATTRIBUTE_SEPARATOR).append("\n");
        }
        eventText.deleteCharAt(eventText.lastIndexOf(EVENT_ATTRIBUTE_SEPARATOR));
        eventText.deleteCharAt(eventText.lastIndexOf("\n"));

        return eventText.toString();
    }
}