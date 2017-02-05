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

package org.wso2.siddhi.extension.output.mapper.map;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.source.OutputMapper;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;
import java.util.TreeMap;

@Extension(
        name = "map",
        namespace = "outputmapper",
        description = ""
)
public class MapOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param options                Custom mapping options
     * @param unmappedDynamicOptions Unmapped dynamic options
     */
    @Override
    public void init(StreamDefinition streamDefinition, Map<String, String> options, Map<String, String> unmappedDynamicOptions) {
        this.streamDefinition = streamDefinition;
    }

    /**
     * Convert the given {@link Event} to Map Object
     *
     * @param event          Event object
     * @param dynamicOptions Dynamic options
     * @return the constructed Map Object
     */
    @Override
    public Object convertToTypedInputEvent(Event event, Map<String, String> dynamicOptions) {
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
                // Add arbitrary data map to the default template
                eventMapObject.put(entry.getKey(), entry.getValue());
            }
        }
        return eventMapObject;
    }

    /**
     * Convert the given option mapping to a Map object
     *
     * @param event            Event object
     * @param mappedPayload Event mapping string array
     * @param dynamicOptions   Dynamic options
     * @return the mapped string
     */
    // Note: Currently, we do not support custom mapping for "map" type since the expected usecases such as renaming and
    // extracting desired attributes can be handled by writing siddhi queries
    @Override
    public Object convertToMappedInputEvent(Event event, String mappedPayload, Map<String, String> dynamicOptions) {
        return mappedPayload;
    }
}
