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
import org.wso2.siddhi.core.stream.input.source.OutputMapper;
import org.wso2.siddhi.core.stream.input.source.OutputTransport;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public class PublishStreamCallback extends OutputCallback {
    private static final Logger log = Logger.getLogger(PublishStreamCallback.class);
    private StreamDefinition outputStreamDefinition;
    private OutputTransport outputTransport;
    private OutputMapper outputMapper;

    public PublishStreamCallback(OutputTransport outputTransport, StreamDefinition outputStreamDefinition) {
        this.outputTransport = outputTransport;
        this.outputMapper = outputTransport.getMapper();
        this.outputStreamDefinition = outputStreamDefinition;
    }

    public void init(ExecutionPlanContext executionPlanContext) {
        // there's nothing to be done, since we moved the
        // type validation mechanism to the transport itself.
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
