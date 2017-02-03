/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.output.mapper.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.publisher.OutputMapper;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

@Extension(
        name = "json",
        namespace = "outputmapper",
        description = ""
)
public class JSONOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    private static final String EVENT_PARENT_TAG = "event";
    private static final String EVENT_ARBITRARY_DATA_MAP_TAG = "arbitraryDataMap";
    private static final String EVENT_ATTRIBUTE_SEPARATOR = ",";

    /**
     * Initialize the mapper and the mapping configurations.
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
     * Convert the given {@link Event} to JSON string
     *
     * @param event          Event object
     * @param dynamicOptions Dynamic options
     * @return the constructed JSON string
     */
    @Override
    public Object convertToTypedInputEvent(Event event, Map<String, String> dynamicOptions) {
        Object[] data = event.getData();
        JsonObject jsonEventObject = new JsonObject();
        JsonObject innerParentObject = new JsonObject();

        for (int i = 0; i < data.length; i++) {
            String attributeName = streamDefinition.getAttributeNameArray()[i];
            Object attributeValue = data[i];
            if (attributeValue != null) {
                if (attributeValue instanceof String) {
                    innerParentObject.addProperty(attributeName, attributeValue.toString());
                } else if (attributeValue instanceof Integer) {
                    innerParentObject.addProperty(attributeName, (Number) attributeValue);
                } else if (attributeValue instanceof Long) {
                    innerParentObject.addProperty(attributeName, (Number) attributeValue);
                } else if (attributeValue instanceof Float) {
                    innerParentObject.addProperty(attributeName, (Number) attributeValue);
                } else if (attributeValue instanceof Double) {
                    innerParentObject.addProperty(attributeName, (Number) attributeValue);
                } else if (attributeValue instanceof Long) {
                    innerParentObject.addProperty(attributeName, (Number) attributeValue);
                } else if (attributeValue instanceof Boolean) {
                    innerParentObject.addProperty(attributeName, (Boolean) attributeValue);
                }
            }
        }

        jsonEventObject.add(EVENT_PARENT_TAG, innerParentObject);

        String eventText = jsonEventObject.toString();

        // Get arbitrary data from event
        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
            // Add arbitrary data map to the default template
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(eventText).getAsJsonObject();
            jsonObject.getAsJsonObject(EVENT_PARENT_TAG).add(EVENT_ARBITRARY_DATA_MAP_TAG, gson.toJsonTree(arbitraryDataMap));
            eventText = gson.toJson(jsonObject);
        }

        return eventText;
    }

    /**
     * Convert the given Event mapping to JSON string
     *
     * @param event            Event object
     * @param mappedAttributes Event mapping string array
     * @param dynamicOptions   Dynamic options
     * @return the mapped JSON string
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
