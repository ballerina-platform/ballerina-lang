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
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.Map;

/**
 * Implementation of {@link Source} to receive events through in-memory transport.
 */
@Extension(
        name = "inMemory",
        namespace = "source",
        description = "In-memory source that can communicate with other in-memory sinks within the same JVM, it " +
                "is assumed that the publisher and subscriber of a topic uses same event schema (stream definition).",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Subscribes to sent on the "
                + "given topic."),
        examples = @Example(
                syntax = "@source(type='inMemory', @map(type='passThrough'),\n" +
                        "define stream BarStream (symbol string, price float, volume long)",
                description = "In this example BarStream uses inMemory transport which passes the received event " +
                        "internally without using external transport."
        )
)
public class InMemorySource extends Source {
    private static final Logger LOG = Logger.getLogger(InMemorySource.class);
    private static final String TOPIC_KEY = "topic";
    private SourceEventListener sourceEventListener;
    private InMemoryBroker.Subscriber subscriber;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        this.sourceEventListener = sourceEventListener;
        String topic = optionHolder.validateAndGetStaticValue(TOPIC_KEY, "input inMemory source");
        this.subscriber = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object event) {
                sourceEventListener.onEvent(event, null);
            }

            @Override
            public String getTopic() {
                return topic;
            }
        };
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
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

    @Override
    public void pause() {
        InMemoryBroker.unsubscribe(subscriber);
    }

    @Override
    public void resume() {
        InMemoryBroker.subscribe(subscriber);
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        // no state
    }
}
