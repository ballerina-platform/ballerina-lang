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
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public abstract class OutputMapper {
    private String type;
    private OptionHolder optionHolder;
    private TemplateBuilder payloadTemplateBuilder = null;

    public final void init(StreamDefinition streamDefinition,
                           String type,
                           OptionHolder optionHolder, String unmappedPayload) {
        this.optionHolder = optionHolder;
        this.type = type;
        if (unmappedPayload != null && !unmappedPayload.isEmpty()) {
            payloadTemplateBuilder = new TemplateBuilder(streamDefinition, unmappedPayload);
        }
        init(streamDefinition, optionHolder, payloadTemplateBuilder);
    }

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition The stream definition
     * @param optionHolder     Option holder containing static and dynamic options
     * @param payloadTemplateBuilder un mapped payload for reference
     */
    public abstract void init(StreamDefinition streamDefinition,
                              OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder);

    public void mapAndSend(Event[] events, OutputTransportCallback outputTransportCallback)
            throws ConnectionUnavailableException{
        mapAndSend(events, outputTransportCallback, optionHolder, payloadTemplateBuilder);
    }

    public abstract void mapAndSend(Event[] events, OutputTransportCallback outputTransportCallback,
                                    OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder)
            throws ConnectionUnavailableException;

    public final String getType() {
        return this.type;
    }

}
