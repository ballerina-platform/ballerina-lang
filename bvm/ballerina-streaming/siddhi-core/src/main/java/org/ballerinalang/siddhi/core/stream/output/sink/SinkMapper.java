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

package org.ballerinalang.siddhi.core.stream.output.sink;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.transport.DynamicOptions;
import org.ballerinalang.siddhi.core.util.transport.OptionHolder;
import org.ballerinalang.siddhi.core.util.transport.TemplateBuilder;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract parent class to represent event mappers. Events mappers will receive {@link Event}s and can convert them
 * to any desired object type (Ex: XML, JSON). Custom mappers can be implemented as extensions extending this
 * abstract implementation.
 */
public abstract class SinkMapper {
    private String type;
    private SinkListener sinkListener;
    private OptionHolder optionHolder;
    //Key as specified in Sink Mapper's payload - @paylod(key="template"), Value is TemplateBuilder for given template
    private Map<String, TemplateBuilder> templateBuilderMap = null;
    private OutputGroupDeterminer groupDeterminer = null;
    private ThreadLocal<DynamicOptions> trpDynamicOptions = new ThreadLocal<>();
    private LatencyTracker mapperLatencyTracker;
    private SiddhiAppContext siddhiAppContext;

    public final void init(StreamDefinition streamDefinition,
                           String type,
                           OptionHolder mapOptionHolder,
                           List<Element> unmappedPayloadList,
                           Sink sink, ConfigReader mapperConfigReader,
                           LatencyTracker mapperLatencyTracker,
                           SiddhiAppContext siddhiAppContext) {
        this.mapperLatencyTracker = mapperLatencyTracker;
        this.siddhiAppContext = siddhiAppContext;
        sink.setTrpDynamicOptions(trpDynamicOptions);
        this.sinkListener = sink;
        this.optionHolder = mapOptionHolder;
        this.type = type;
        if (unmappedPayloadList != null && !unmappedPayloadList.isEmpty()) {
            templateBuilderMap = new HashMap<>();
            for (Element e : unmappedPayloadList) {
                TemplateBuilder templateBuilder = new TemplateBuilder(streamDefinition, e.getValue());
                if (templateBuilderMap.containsKey(e.getKey())) {
                    throw new SiddhiAppCreationException("Duplicate Keys, " + e.getKey() + ", in @payload() ");
                }
                templateBuilderMap.put(e.getKey(), templateBuilder);
            }
        }

        init(streamDefinition, mapOptionHolder, templateBuilderMap, mapperConfigReader, siddhiAppContext);
    }

    /**
     * Supported dynamic options by the mapper.
     *
     * @return the list of supported dynamic option keys
     */
    public abstract String[] getSupportedDynamicOptions();


    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition          The stream definition
     * @param optionHolder              Option holder containing static and dynamic options related to the mapper
     * @param payloadTemplateBuilderMap Un mapped payloads for reference
     * @param mapperConfigReader        System configuration reader for Sink-mapper.
     * @param siddhiAppContext          Siddhi Application Context
     */
    public abstract void init(StreamDefinition streamDefinition,
                              OptionHolder optionHolder,
                              Map<String, TemplateBuilder> payloadTemplateBuilderMap,
                              ConfigReader mapperConfigReader,
                              SiddhiAppContext siddhiAppContext);

    /**
     * Get produced event class types.
     *
     * @return Array of classes that will be produced by the sink-mapper,
     * null or empty array if it can produce any type of class.
     */
    public abstract Class[] getOutputEventClasses();

    /**
     * Called to map the events and send them to {@link SinkListener} for publishing.
     *
     * @param events {@link Event}s that need to be mapped
     */
    final void mapAndSend(Event[] events) {
        if (groupDeterminer != null) {
            LinkedHashMap<String, ArrayList<Event>> eventMap = new LinkedHashMap<>();
            for (Event event : events) {
                String key = groupDeterminer.decideGroup(event);
                ArrayList<Event> eventList = eventMap.computeIfAbsent(key, k -> new ArrayList<>());
                eventList.add(event);
            }
            for (ArrayList<Event> eventList : eventMap.values()) {
                try {
                    trpDynamicOptions.set(new DynamicOptions(eventList.get(0)));
                    if (mapperLatencyTracker != null && siddhiAppContext.isStatsEnabled()) {
                        mapperLatencyTracker.markIn();
                    }
                    mapAndSend(eventList.toArray(new Event[eventList.size()]), optionHolder, templateBuilderMap,
                            sinkListener);
                } finally {
                    trpDynamicOptions.remove();
                    if (mapperLatencyTracker != null && siddhiAppContext.isStatsEnabled()) {
                        mapperLatencyTracker.markOut();
                    }
                }
            }
        } else {
            try {
                trpDynamicOptions.set(new DynamicOptions(events[0]));
                if (mapperLatencyTracker != null && siddhiAppContext.isStatsEnabled()) {
                    mapperLatencyTracker.markIn();
                }
                mapAndSend(events, optionHolder, templateBuilderMap, sinkListener);
            } finally {
                trpDynamicOptions.remove();
                if (mapperLatencyTracker != null && siddhiAppContext.isStatsEnabled()) {
                    mapperLatencyTracker.markOut();
                }
            }
        }
    }


    /**
     * Called to map the event and send it to {@link SinkListener} for publishing.
     *
     * @param event The {@link Event} that need to be mapped
     */
    final void mapAndSend(Event event) {
        try {
            trpDynamicOptions.set(new DynamicOptions(event));
            mapAndSend(event, optionHolder, templateBuilderMap, sinkListener);
        } finally {
            trpDynamicOptions.remove();

        }
    }

    /**
     * Called to map the events and send them to {@link SinkListener} for publishing.
     *
     * @param events                    {@link Event}s that need to be mapped
     * @param optionHolder              Option holder containing static and dynamic options related to the mapper
     * @param payloadTemplateBuilderMap To build the message payloads based on the given templates
     * @param sinkListener              {@link SinkListener} that will be called with the mapped events
     */
    public abstract void mapAndSend(Event[] events, OptionHolder optionHolder, Map<String, TemplateBuilder>
            payloadTemplateBuilderMap, SinkListener sinkListener);

    /**
     * Called to map the event and send it to {@link SinkListener} for publishing.
     *
     * @param event                     {@link Event} that need to be mapped
     * @param optionHolder              Option holder containing static and dynamic options related to the mapper
     * @param payloadTemplateBuilderMap To build the message payloads based on the given templates
     * @param sinkListener              {@link SinkListener} that will be called with the mapped event
     */
    public abstract void mapAndSend(Event event, OptionHolder optionHolder, Map<String,
            TemplateBuilder> payloadTemplateBuilderMap, SinkListener sinkListener);

    public final String getType() {
        return this.type;
    }

    public final void setGroupDeterminer(OutputGroupDeterminer groupDeterminer) {
        this.groupDeterminer = groupDeterminer;
    }

}
