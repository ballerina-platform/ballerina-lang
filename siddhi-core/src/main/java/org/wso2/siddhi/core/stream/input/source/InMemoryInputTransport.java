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
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.core.util.transport.OptionHolder;

@Extension(
        name = "inMemory",
        namespace = "inputtransport",
        description = "In-memory transport that can communicate with other in-memory transports within the same JVM, it " +
                "is assumed that the publisher and subscriber of a topic uses same event schema (stream definition).",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Subscribes to sent on the given" +
                " topic.")
)
public class InMemoryInputTransport extends InputTransport {
    private static final Logger log = Logger.getLogger(InMemoryInputTransport.class);
    private static final String TOPIC_KEY = "topic";
    private SourceCallback sourceCallback;
    private InMemoryBroker.Subscriber subscriber;

    @Override
    public void init(SourceCallback sourceCallback, OptionHolder optionHolder) {
        this.sourceCallback = sourceCallback;
        String topic = optionHolder.validateAndGetStaticValue(TOPIC_KEY,"input inMemory transport");
        this.subscriber = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object event) {
                sourceCallback.onEvent(event);
            }

            @Override
            public String getTopic() {
                return topic;
            }
        };
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        InMemoryBroker.subscribe(subscriber);
    }

    @Override
    public void disconnect() {
        InMemoryBroker.unsubscribe(subscriber);
    }

    @Override
    public void destroy() {
        // do nothing
    }

}
