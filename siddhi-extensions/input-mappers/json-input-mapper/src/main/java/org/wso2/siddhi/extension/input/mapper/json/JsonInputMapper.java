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
package org.wso2.siddhi.extension.input.mapper.json;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.Sourcemapper;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.util.AttributeConverter;
import org.wso2.siddhi.core.util.transport.AttributeMapping;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

/**
 * This mapper converts JSON string input to {@link ComplexEventChunk}. This extension accepts optional mapping to
 * select
 * specific attributes from the stream.
 * For example, <pre>{@code subscription.map(Mapping.format("json"));}</pre> converts a given JSON string to Java
 * objects and select the stream attributes based on the output stream's attributes.
 * If custom mapping is given like <pre>{@code subscription.map(Mapping.format("json").map("$.country").map("$
 * .price").map("$.volume", "volume"));}</pre>, it will select the user defined attributes only.
 */
@Extension(
        name = "json",
        namespace = "sourceMapper",
        description = ""
)
public class JsonSourcemapper implements Sourcemapper {

    /**
     * Logger to log the events.
     */
    private static final Logger log = Logger.getLogger(JsonSourcemapper.class);

    /**
     * JSON path interprets the root of a JSON object as $.
     */
    public static final String DEFAULT_JSON_MAPPING_PREFIX = "$.";
    /**
     * Logger to log the events.
     */
    private static final Logger log = Logger.getLogger(JsonInputMapper.class);
    /**
     * Output StreamDefinition of the input mapper.
     */
    private StreamDefinition streamDefinition;

    /**
     * Array of information about event mapping.
     */
    private MappingPositionData[] mappingPositions;

    /**
     * Attributes of the output stream.
     */
    private List<Attribute> streamAttributes;

    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder mapOptionHolder) {
        this.streamDefinition = streamDefinition;
        this.streamAttributes = this.streamDefinition.getAttributeList();

        int attributesSize = this.streamDefinition.getAttributeList().size();
        this.mappingPositions = new MappingPositionData[attributesSize];

        // Create the position mapping arrays
        if (streamAttributes.size() > 0) {
            // Custom mapping parameters are given
            for (int i = 0; i < streamAttributes.size(); i++) {
                // i represents the position of attributes as given by the user in mapping

                AttributeMapping attributeMapping = streamAttributes.get(i);

                // The optional name given by the user in attribute mapping
                String attributeName = attributeMapping.getRename();

                // The position of the attribute in the output stream definition
                int position;

                if (attributeName != null) {
                    // Use the name to determine the position
                    position = this.streamDefinition.getAttributePosition(attributeName);
                } else {
                    // Use the same order as provided by the user
                    position = i;
                }
                this.mappingPositions[i] = new MappingPositionData(position, attributeMapping.getMapping());
            }
        } else {
            // Use the attribute names of the output stream in order
            for (int i = 0; i < attributesSize; i++) {
                this.mappingPositions[i] = new MappingPositionData(i, DEFAULT_JSON_MAPPING_PREFIX + this
                        .streamDefinition.getAttributeList().get(i).getName());
            }
        }
    }

    /**
     * Receive JSON string from {@link Source}, convert to {@link ComplexEventChunk} and send to the
     * {@link OutputCallback}.
     *
     * @param eventObject the JSON string
     */
    @Override
    public void onEvent(Object eventObject) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(convertToEvent(eventObject), borrowedEvent);
        outputCallback.send(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, true));
    }

    @Override
    protected void mapAndProcess(Object eventObject, InputHandler inputHandler) {

    }

    /**
     * Convert the given JSON string to {@link Event}
     *
     * @param eventObject JSON string
     * @return the constructed Event object
     */
    private Event convertToEvent(Object eventObject) {

        // Validate the event
        if (eventObject == null) {
            throw new ExecutionPlanRuntimeException("Null object received from the Source to JsonSourcemapper");
        }

        if (!(eventObject instanceof String)) {
            throw new ExecutionPlanRuntimeException("Invalid JSON object received. Expected String, but found " +
                    eventObject.getClass()
                            .getCanonicalName());
        }

        Event event = new Event(this.streamDefinition.getAttributeList().size());
        Object[] data = event.getData();

        // Parse the JSON string and build the data
        ReadContext readContext = JsonPath.parse(eventObject.toString());
        for (MappingPositionData mappingPositionData : this.mappingPositions) {
            int position = mappingPositionData.getPosition();
            data[position] = AttributeConverter.getPropertyValue(readContext.read(mappingPositionData.getMapping()),
                    streamAttributes.get(position).getType());
        }

        return event;
    }

    /**
     * A POJO class which holds the attribute position in output stream and the user defined mapping.
     */
    private class MappingPositionData {
        /**
         * Attribute position in the output stream.
         */
        private int position;

        /**
         * The JSON mapping as defined by the user.
         */
        private String mapping;

        public MappingPositionData(int position, String mapping) {
            this.position = position;
            this.mapping = mapping;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getMapping() {
            return mapping;
        }

        public void setMapping(String mapping) {
            this.mapping = mapping;
        }
    }
}
