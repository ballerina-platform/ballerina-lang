/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.output.callback;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.OutputTransportException;
import org.wso2.siddhi.core.publisher.OutputMapper;
import org.wso2.siddhi.core.publisher.OutputTransport;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;

public class PublishStreamCallback extends OutputCallback {
    private static final Logger log = Logger.getLogger(PublishStreamCallback.class);
    private StreamDefinition outputStreamDefinition;
    private OutputTransport outputTransport;
    private Transport transportConfig;
    private OutputMapper outputMapper;
    private Mapping mappingConfig;

    public PublishStreamCallback(OutputTransport outputTransport, Transport transportConfig,
                                 OutputMapper outputMapper, Mapping mappingConfig,
                                 StreamDefinition outputStreamDefinition) {
        this.outputTransport = outputTransport;
        this.transportConfig = transportConfig;
        this.outputMapper = outputMapper;
        this.mappingConfig = mappingConfig;
        this.outputStreamDefinition = outputStreamDefinition;
    }

    public void init(ExecutionPlanContext executionPlanContext) {
        try {
            // validateSupportedMapping
            if (outputTransport.isMessageFormatSupported(mappingConfig.getFormat())) {
                outputMapper.init(outputStreamDefinition, mappingConfig);
                outputTransport.init(executionPlanContext, outputStreamDefinition, transportConfig);
            } else {
                throw new ExecutionPlanValidationException(String.format("%s mapping is not supported by " +
                        "transport type %s", mappingConfig.getFormat(), transportConfig.getType()));
            }
        } catch (OutputTransportException e) {
            log.error("Error when initializing output transport.", e);
        }
    }

    @Override
    public void send(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        ComplexEvent complexEvent = complexEventChunk.getFirst();
        while (complexEvent != null) {
            try {
                Event event = new Event(complexEvent.getOutputData().length).copyFrom(complexEvent);
                outputTransport.publish(
                        outputMapper.mapEvent(event),
                        outputTransport.getDynamicOptions(event)
                );
            } catch (ConnectionUnavailableException e) {
                log.error("Cannot publish to Output Transport due to unavailability of connection.", e);
            }
            complexEvent = complexEvent.getNext();
        }
    }
}
