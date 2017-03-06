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

package org.wso2.siddhi.extension.output.mapper.keyvalue;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.stream.output.sink.OutputTransportCallback;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;

@Extension(
        name = "keyvalue",
        namespace = "outputmapper",
        description = "Event to key-value (HashMap) output mapper."
)
public class KeyValueOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;

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
        //TODO add support to publish multiple events
        for (Event event : events) {
            outputTransportCallback.publish(constructDefaultMapping(event), event);
        }
    }

    /**
     * Convert the given {@link Event} to HashMap
     *
     * @param event Event object
     * @return the constructed HashMap
     */
    private Object constructDefaultMapping(Event event) {
        Map<Object, Object> eventMapObject = new HashMap<Object, Object>();
        Object[] eventData = event.getData();
        for (int i = 0; i < eventData.length; i++) {
            String attributeName = streamDefinition.getAttributeNameArray()[i];
            Object attributeValue = eventData[i];
            eventMapObject.put(attributeName, attributeValue);
        }

        // TODO remove if we are not going to support arbitraryDataMap
/*        // Get arbitrary data from event
        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null) {
            for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                // Add arbitrary data keyvalue to the default template
                eventMapObject.put(entry.getKey(), entry.getValue());
            }
        }*/
        return eventMapObject;
    }
}
