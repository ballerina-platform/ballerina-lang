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
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;
import java.util.TreeMap;

@Extension(
        name = "keyvalue",
        namespace = "outputmapper",
        description = "Event to key-value (HashMap) output mapper."
)
public class MapOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;

    /**
     * Initialize the mapper and the mapping configurations.
     *  @param streamDefinition The stream definition
     * @param optionHolder     Unmapped dynamic options
     * @param payloadTemplateBuilder
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder) {
        this.streamDefinition = streamDefinition;
    }

    /**
     * Convert the given option mapping to a HashMap object
     *
     * @param event         Event object
     * @param mappedPayload mapped payload if any
     * @return the mapped object
     */
    @Override
    public Object mapEvent(Event event, String mappedPayload) {
        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        Object[] eventData = event.getData();
        for (int i = 0; i < eventData.length; i++) {
            String attributeName = streamDefinition.getAttributeNameArray()[i];
            Object attributeValue = eventData[i];
            eventMapObject.put(attributeName, attributeValue);
        }

        // Get arbitrary data from event
        Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null) {
            for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                // Add arbitrary data keyvalue to the default template
                eventMapObject.put(entry.getKey(), entry.getValue());
            }
        }
        return eventMapObject;
    }
}
