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

package org.wso2.siddhi.core.util.transport;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.stream.output.sink.OutputTransport;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

/**
 * InMemoryOutputTransport will be used to publish events internally.
 * To use with testing, manually register this extension to {@link org.wso2.siddhi.core.SiddhiManager}
 * siddhiManager.setExtension("outputtransport:inMemory", InMemoryOutputTransport.class);
 */
public class InMemoryOutputTransport extends OutputTransport {
    private static final Logger log = Logger.getLogger(InMemoryOutputTransport.class);
    private static final String TOPIC_KEY = "topic";

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder) {
        if (!optionHolder.containsOption(TOPIC_KEY)) {
            throw new ExecutionPlanCreationException(String.format("{{%s}} configuration " +
                    "could not be found in provided configs.", TOPIC_KEY));
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        // do nothing
    }

    @Override
    public void disconnect() {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }

    @Override
    protected void publish(Object payload, Event event, OptionHolder optionHolder) throws ConnectionUnavailableException {
        InMemoryBroker.publish(optionHolder.getStaticOption(TOPIC_KEY), event);
    }
}