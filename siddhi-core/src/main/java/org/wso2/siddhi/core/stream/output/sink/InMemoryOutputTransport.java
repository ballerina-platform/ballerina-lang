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

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

@Extension(
        name = "inMemory",
        namespace = "outputtransport",
        description = "In-memory transport that can communicate with other in-memory transports within the same JVM, it" +
                "is assumed that the publisher and subscriber of a topic uses same event schema (stream definition).",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Event will be delivered to all" +
                "the subscribers of the same topic")
)
public class InMemoryOutputTransport extends OutputTransport {
    private static final Logger log = Logger.getLogger(InMemoryOutputTransport.class);
    private static final String TOPIC_KEY = "topic";
    private Option topicOption;

    @Override
    protected String[] init(StreamDefinition streamDefinition, OptionHolder optionHolder) {
        topicOption = optionHolder.validateAndGetOption(TOPIC_KEY);
        if(!topicOption.isStatic()){
            return new String[]{topicOption.getKey()};
        }
        return new String[0];
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
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        InMemoryBroker.publish(topicOption.getValue(dynamicOptions), payload);
    }

}