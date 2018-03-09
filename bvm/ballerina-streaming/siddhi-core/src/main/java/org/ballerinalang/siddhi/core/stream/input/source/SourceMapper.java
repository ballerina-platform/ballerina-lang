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

package org.ballerinalang.siddhi.core.stream.input.source;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.core.util.transport.OptionHolder;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Convert custom input from {@link Source} to {@link org.ballerinalang.siddhi.core.event.ComplexEventChunk}.
 */
public abstract class SourceMapper implements SourceEventListener {

    private static final Logger log = LoggerFactory.getLogger(SourceMapper.class);
    private final ThreadLocal<String[]> trpProperties = new ThreadLocal<>();
    private InputEventHandler inputEventHandler;
    private StreamDefinition streamDefinition;
    private String mapType;
    private String sourceType;
    private List<AttributeMapping> transportMappings;
    private SourceHandler sourceHandler;
    private SiddhiAppContext siddhiAppContext;
    private ThroughputTracker throughputTracker;
    private LatencyTracker mapperLatencyTracker;

    public final void init(StreamDefinition streamDefinition, String mapType, OptionHolder mapOptionHolder,
                           List<AttributeMapping> attributeMappings, String sourceType,
                           List<AttributeMapping> transportMappings, SourceHandler sourceHandler,
                           ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.streamDefinition = streamDefinition;
        this.mapType = mapType;
        this.sourceType = sourceType;
        this.transportMappings = transportMappings;
        if (sourceHandler != null) {
            sourceHandler.initSourceHandler(siddhiAppContext.getElementIdGenerator().createNewId(), streamDefinition);
        }
        this.sourceHandler = sourceHandler;
        this.siddhiAppContext = siddhiAppContext;
        if (siddhiAppContext.getStatisticsManager() != null) {
            this.throughputTracker = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    streamDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_SOURCES, sourceType);
            this.mapperLatencyTracker = QueryParserHelper.createLatencyTracker(siddhiAppContext,
                    streamDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_SOURCE_MAPPERS,
                    sourceType + SiddhiConstants.METRIC_DELIMITER + mapType);
        }
        init(streamDefinition, mapOptionHolder, attributeMappings, configReader, siddhiAppContext);
    }

    /**
     * Initialize Source-mapper.
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

    public final void setInputHandler(InputHandler inputHandler) {
        InputEventHandlerCallback inputEventHandlerCallback;
        if (sourceHandler != null) {
            sourceHandler.setInputHandler(inputHandler);
            inputEventHandlerCallback = sourceHandler;
        } else {
            inputEventHandlerCallback = new PassThroughSourceHandler(inputHandler);
        }
        LatencyTracker mapperLatencyTracker = null;
        this.inputEventHandler = new InputEventHandler(inputHandler, transportMappings,
                trpProperties, sourceType, mapperLatencyTracker, siddhiAppContext,
                inputEventHandlerCallback);
    }

    public final void onEvent(Object eventObject, String[] transportProperties) {
        try {
            if (eventObject != null) {
                if (!allowNullInTransportProperties() && transportProperties != null) {
                    for (String property : transportProperties) {
                        if (property == null) {
                            log.error("Dropping event " + eventObject.toString() + " belonging to stream " +
                                    sourceHandler.getInputHandler().getStreamId()
                                    + " as it contains null transport properties and system "
                                    + "is configured to not allow null transport properties. You can "
                                    + "configure it via source mapper if the respective "
                                    + "mapper type allows it. Refer mapper documentation to verify "
                                    + "supportability");
                            return;
                        }
                    }
                }
                trpProperties.set(transportProperties);
                try {
                    if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
                        throughputTracker.eventIn();
                        mapperLatencyTracker.markIn();
                    }
                    mapAndProcess(eventObject, inputEventHandler);
                } finally {
                    if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
                        mapperLatencyTracker.markOut();
                    }
                }
            }
        } catch (InterruptedException | RuntimeException e) {
            log.error("Error while processing '" + eventObject + "', for the input Mapping '" + mapType +
                    "' for the stream '" + streamDefinition.getId() + "'");
        } finally {
            trpProperties.remove();
        }
    }

    public SourceHandler getHandler() {
        return this.sourceHandler;
    }

    public final StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    /**
     * Method to map the incoming event and as pass that via inputEventHandler to process further.
     *
     * @param eventObject       Incoming event Object
     * @param inputEventHandler Handler to pass the converted Siddhi Event for processing
     * @throws InterruptedException Throws InterruptedException
     */
    protected abstract void mapAndProcess(Object eventObject,
                                          InputEventHandler inputEventHandler)
            throws InterruptedException;

    /**
     * Method used by {@link SourceMapper} to determine on how to handle transport properties with null values. If
     * this returns 'false' then {@link SourceMapper} will drop any event/s with null transport
     * property values. If this returns
     * 'true' then {@link SourceMapper} will send events even though they contains null transport properties.
     * This method will be called after init().
     *
     * @return whether {@link SourceMapper} should allow or drop events when transport properties are null.
     */
    protected abstract boolean allowNullInTransportProperties();
}
