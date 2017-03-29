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
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.stream.output.sink.OutputTransportCallback;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

@Extension(
        name = "json",
        namespace = "outputmapper",
        description = "Event to JSON output mapper."
)
public class JSONOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    private static final String GROUP_EVENTS_OPTION = "groupEvents";
    private static final String EVENTS_PARENT_TAG = "events";
    private static final String EVENT_PARENT_TAG = "event";
    private static final String EVENT_ARBITRARY_DATA_MAP_TAG = "arbitraryDataMap";
    //Todo  use group events to group multiple events.
    boolean groupEvents = false;


    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param optionHolder           Option holder containing static and dynamic options
     * @param payloadTemplateBuilder Unmapped payload for reference
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder) {
        this.streamDefinition = streamDefinition;
        ///TODO 2/3/2017 Fix this to support grouping events
//        String groupEventsString = optionHolder.getStaticOption(GROUP_EVENTS_OPTION);
//        if (groupEventsString != null) {
//            groupEvents = Boolean.parseBoolean(groupEventsString);
//        }
    }

    /**
     * Map and publish the given {@link Event} array
     *
     * @param events                  Event object array
     * @param outputTransportCallback output transport callback
     * @param optionHolder            option holder containing static and dynamic options
     * @param payloadTemplateBuilder  Unmapped payload for reference
     */
    @Override
    public void mapAndSend(Event[] events, OutputTransportCallback outputTransportCallback,
                           OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder)
            throws ConnectionUnavailableException {

        if (payloadTemplateBuilder != null) {
            for (Event event : events) {
                outputTransportCallback.publish(payloadTemplateBuilder.build(event), event);
            }
        } else {
            //TODO add support to publish multiple events
            for (Event event : events) {
                outputTransportCallback.publish(constructDefaultMapping(event), event);
            }
        }
    }


    /**
     * Convert the given {@link Event} to JSON string
     *
     * @param event Event object
     * @return the constructed JSON string
     */
    private Object constructDefaultMapping(Event event) {
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
                } else if (attributeValue instanceof Boolean) {
                    innerParentObject.addProperty(attributeName, (Boolean) attributeValue);
                } else if (attributeValue instanceof Map) {
                    if (!((Map) attributeValue).isEmpty()) {
                        Gson gson = new Gson();
                        innerParentObject.add(attributeName, gson.toJsonTree((Map) attributeValue));
                    }
                }
            }
        }

        jsonEventObject.add(EVENT_PARENT_TAG, innerParentObject);

        String eventText = jsonEventObject.toString();

//        // Get arbitrary data from event
//        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
//        if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
//            // Add arbitrary data map to the default template
//            Gson gson = new Gson();
//            JsonParser parser = new JsonParser();
//            JsonObject jsonObject = parser.parse(eventText).getAsJsonObject();
//            jsonObject.getAsJsonObject(EVENT_PARENT_TAG).add(EVENT_ARBITRARY_DATA_MAP_TAG, gson.toJsonTree(arbitraryDataMap));
//            eventText = gson.toJson(jsonObject);
//        }

        return eventText;
    }


}
