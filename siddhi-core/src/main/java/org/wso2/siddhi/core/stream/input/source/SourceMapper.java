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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

/**
 * Convert custom input from {@link Source} to {@link org.wso2.siddhi.core.event.ComplexEventChunk}.
 */
public abstract class SourceMapper implements SourceEventListener {

    private static final Logger log = Logger.getLogger(SourceMapper.class);
    private final ThreadLocal<String[]> trpProperties = new ThreadLocal<>();
    private InputEventHandler inputEventHandler;
    private StreamDefinition streamDefinition;
    private String mapType;
    private String sourceType;
    private List<AttributeMapping> transportMappings;

    public final void init(StreamDefinition streamDefinition, String mapType, OptionHolder mapOptionHolder,
                           List<AttributeMapping> attributeMappings, String sourceType,
                           List<AttributeMapping> transportMappings,
                           ConfigReader configReader,
                           SiddhiAppContext siddhiAppContext) {
        this.streamDefinition = streamDefinition;
        this.mapType = mapType;
        this.sourceType = sourceType;
        this.transportMappings = transportMappings;
        init(streamDefinition, mapOptionHolder, attributeMappings, configReader, siddhiAppContext);
    }

    /**
     * Initialize Source-mapper
     *
     * @param streamDefinition     Associated output stream definition
     * @param optionHolder         Mapper option holder
     * @param attributeMappingList Custom attribute mapping for source-mapping
     * @param configReader         System configuration reader
     * @param siddhiAppContext     Siddhi application context
     */
    public abstract void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping>
            attributeMappingList, ConfigReader configReader, SiddhiAppContext siddhiAppContext);

    /**
     * Support classes that the source-mapper can consume for mapping processing (used for validation purposes)
     *
     * @return Supported event classes that mapper can process.
     */
    public abstract Class[] getSupportedInputEventClasses();

    public final void setInputHandler(InputHandler inputEventHandler) {
        this.inputEventHandler = new InputEventHandler(inputEventHandler, transportMappings, trpProperties, sourceType);
    }

    public final void onEvent(Object eventObject, String[] transportProperties) {
        try {
            if (eventObject != null) {
                trpProperties.set(transportProperties);
                mapAndProcess(eventObject, inputEventHandler);
            }
        } catch (InterruptedException | RuntimeException e) {
            log.error("Error while processing '" + eventObject + "', for the input Mapping '" + mapType +
                    "' for the stream '" + streamDefinition.getId() + "'");
        } finally {
            trpProperties.remove();
        }
    }

    public final StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    /**
     * Method to map the incoming event and as pass that via inputEventHandler to process further.
     *
     * @param eventObject       Incoming event Object
     * @param inputEventHandler Handler to pass the converted Siddhi Event for processing
     * @throws InterruptedException
     */
    protected abstract void mapAndProcess(Object eventObject,
                                          InputEventHandler inputEventHandler)
            throws InterruptedException;
}
