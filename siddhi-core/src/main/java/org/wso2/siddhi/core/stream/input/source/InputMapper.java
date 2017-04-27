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
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

/**
 * Convert custom input from {@link InputTransport} to {@link org.wso2.siddhi.core.event.ComplexEventChunk}.
 */
public abstract class InputMapper implements SourceEventListener {

    private InputHandler inputHandler;
    private StreamDefinition streamDefinition;
    private String mapType;
    private static final Logger log = Logger.getLogger(InputMapper.class);

    public void init(StreamDefinition streamDefinition, String mapType, OptionHolder mapOptionHolder,
                     List<AttributeMapping> attributeMappings) {
        this.streamDefinition = streamDefinition;
        this.mapType = mapType;
        init(streamDefinition, mapOptionHolder, attributeMappings);
    }

    public abstract void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping> attributeMappingList);

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public void onEvent(Object eventObject) {
        try {
            if (eventObject != null) {
                mapAndProcess(eventObject, inputHandler);
            }
        } catch (InterruptedException e) {
            log.error("Error while processing '" + eventObject + "', for the input Mapping '" + mapType +
                    "' for the stream '" + streamDefinition.getId() + "'");
        }
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    protected abstract void mapAndProcess(Object eventObject, InputHandler inputHandler) throws InterruptedException;

}
