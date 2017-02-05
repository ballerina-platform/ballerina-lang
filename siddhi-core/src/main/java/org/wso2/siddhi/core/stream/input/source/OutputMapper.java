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

package org.wso2.siddhi.core.stream.input.source;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;

public abstract class OutputMapper {
    private Converter payloadConverters;
    private Map<String, Converter> dynamicOptionConverters;
    private boolean isCustomMappingEnabled;
    private String format;

    /**
     * This will be called only once and this can be used to acquire
     * required resources for processing the mapping.
     */
    public abstract void init(StreamDefinition streamDefinition,
                              Map<String, String> options,
                              Map<String, String> unmappedDynamicOptions);

    // TODO: 2/1/17 one method instead of two methods
    public abstract Object convertToTypedInputEvent(Event event, Map<String, String> dynamicOptions);

    public abstract Object convertToMappedInputEvent(Event event, String mappedPayload,
                                                     Map<String, String> dynamicOptions);

    public final void init(StreamDefinition streamDefinition, String format, String payload,
                           Map<String, String> options, Map<String, String> dynamicOptions) {
        this.format = format;
        isCustomMappingEnabled = payload != null && !payload.isEmpty();
        if (isCustomMappingEnabled) {
            payloadConverters = new Converter(streamDefinition, payload);
        }

        dynamicOptionConverters = new HashMap<String, Converter>();
        for (Map.Entry<String, String> entry : dynamicOptions.entrySet()) {
            dynamicOptionConverters.put(entry.getKey(),
                    new Converter(streamDefinition, entry.getValue()));
        }

        init(streamDefinition, options, dynamicOptions);
    }

    public final Object mapEvent(Event event) {
        if (isCustomMappingEnabled) {
            return convertToMappedInputEvent(event, mapPayload(event), getMappedOptions(event));
        } else {
            return convertToTypedInputEvent(event, getMappedOptions(event));
        }
    }

    public final String getFormat() {
        return this.format;
    }

    private Map<String, String> getMappedOptions(Event event) {
        return Converter.convert(event, dynamicOptionConverters);
    }

    private String mapPayload(Event event) {
        return payloadConverters.map(event);
    }
}
