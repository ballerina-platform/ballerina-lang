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

package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class OutputMapper {
    private String type;
    private OptionHolder optionHolder;
    private TemplateBuilder payloadTemplateBuilder = null;
    private ArrayList<Option> transportOptions;

    public final void init(StreamDefinition streamDefinition,
                           String type,
                           OptionHolder mapOptionHolder, String unmappedPayload, String[] publishGroupDeterminers,
                           OptionHolder transportOptionHolder) {
        this.optionHolder = mapOptionHolder;
        this.type = type;
        if (unmappedPayload != null && !unmappedPayload.isEmpty()) {
            payloadTemplateBuilder = new TemplateBuilder(streamDefinition, unmappedPayload);
        }
        init(streamDefinition, mapOptionHolder, payloadTemplateBuilder);
        transportOptions = new ArrayList<Option>(publishGroupDeterminers.length);
        for (String publishGroupDeterminer : publishGroupDeterminers) {
            transportOptions.add(transportOptionHolder.validateAndGetOption(publishGroupDeterminer));
        }

    }

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param optionHolder           Option holder containing static and dynamic options related to the mapper
     * @param payloadTemplateBuilder un mapped payload for reference
     */
    public abstract void init(StreamDefinition streamDefinition,
                              OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder);

    /**
     * Called to map the events and send them to {@link OutputTransportListener}
     *
     * @param events                  {@link Event}s that need to be mapped
     * @param outputTransportListener {@link OutputTransportListener} that will be called with the mapped events
     * @throws ConnectionUnavailableException If the connection is not available to send the message
     */
    public void mapAndSend(Event[] events, OutputTransportListener outputTransportListener)
            throws ConnectionUnavailableException {

        if (transportOptions.size() > 0) {
            LinkedHashMap<String, ArrayList<Event>> eventMap = new LinkedHashMap<>();
            for (Event event : events) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Option transportOption : transportOptions) {
                    stringBuilder.append(transportOption.getValue(event));
                    stringBuilder.append(":::");
                }
                String key = stringBuilder.toString();
                ArrayList<Event> eventList = eventMap.computeIfAbsent(key, k -> new ArrayList<>());
                eventList.add(event);
            }
            for (ArrayList<Event> eventList : eventMap.values()) {
                mapAndSend(eventList.toArray(new Event[eventList.size()]), optionHolder,
                        payloadTemplateBuilder, outputTransportListener, new DynamicOptions(eventList.get(0)));

            }
        } else {
            mapAndSend(events, optionHolder, payloadTemplateBuilder, outputTransportListener,
                    new DynamicOptions(events[0]));
        }
    }

    /**
     * Called to map the event and send it to {@link OutputTransportListener}
     *
     * @param event                   The {@link Event} that need to be mapped
     * @param outputTransportListener {@link OutputTransportListener} that will be called with the mapped event
     * @throws ConnectionUnavailableException If the connection is not available to send the message
     */
    public void mapAndSend(Event event, OutputTransportListener outputTransportListener)
            throws ConnectionUnavailableException {
        mapAndSend(event, optionHolder, payloadTemplateBuilder, outputTransportListener, new DynamicOptions(event));
    }

    /**
     * Called to map the events and send them to {@link OutputTransportListener}
     *
     * @param events                  {@link Event}s that need to be mapped
     * @param optionHolder            Option holder containing static and dynamic options related to the mapper
     * @param payloadTemplateBuilder  To build the message payload based on the given template
     * @param outputTransportListener {@link OutputTransportListener} that will be called with the mapped events
     * @param dynamicTransportOptions {@link DynamicOptions} containing transport related options which will be passed
     *                                to the  {@link OutputTransportListener}
     * @throws ConnectionUnavailableException If the connection is not available to send the message
     */
    public abstract void mapAndSend(Event[] events, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder,
                                    OutputTransportListener outputTransportListener, DynamicOptions dynamicTransportOptions)
            throws ConnectionUnavailableException;

    /**
     * Called to map the event and send it to {@link OutputTransportListener}
     *
     * @param event                   {@link Event} that need to be mapped
     * @param optionHolder            Option holder containing static and dynamic options related to the mapper
     * @param payloadTemplateBuilder  To build the message payload based on the given template
     * @param outputTransportListener {@link OutputTransportListener} that will be called with the mapped event
     * @param dynamicTransportOptions {@link DynamicOptions} containing transport related options which will be passed
     *                                to the  {@link OutputTransportListener}
     * @throws ConnectionUnavailableException If the connection is not available to send the message
     */
    public abstract void mapAndSend(Event event, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder,
                                    OutputTransportListener outputTransportListener, DynamicOptions dynamicTransportOptions)
            throws ConnectionUnavailableException;

    public final String getType() {
        return this.type;
    }

}
