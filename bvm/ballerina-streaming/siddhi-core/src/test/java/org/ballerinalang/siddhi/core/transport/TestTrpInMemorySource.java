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

package org.ballerinalang.siddhi.core.transport;

import org.ballerinalang.siddhi.annotation.Example;
import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.annotation.Parameter;
import org.ballerinalang.siddhi.annotation.util.DataType;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.stream.input.source.InMemorySource;
import org.ballerinalang.siddhi.core.stream.input.source.SourceEventListener;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.core.util.transport.InMemoryBroker;
import org.ballerinalang.siddhi.core.util.transport.OptionHolder;

/**
 * Implementation of {@link org.ballerinalang.siddhi.core.stream.input.source.Source} to receive events
 * through in-memory transport.
 */
@Extension(
        name = "testTrpInMemory",
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
public class TestTrpInMemorySource extends InMemorySource {
    private static final String TOPIC_KEY = "topic";
    protected SourceEventListener sourceEventListener;
    protected InMemoryBroker.Subscriber subscriber;
    private String prop1;
    private String prop2;
    private String fail;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder, String[]
            requestedTransportPropertyNames, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        super.init(sourceEventListener, optionHolder, requestedTransportPropertyNames, configReader, siddhiAppContext);
        prop1 = optionHolder.validateAndGetStaticValue("prop1");
        prop2 = optionHolder.validateAndGetStaticValue("prop2");
        fail = optionHolder.validateAndGetStaticValue("fail", "false");
        this.sourceEventListener = sourceEventListener;
        String topic = optionHolder.validateAndGetStaticValue(TOPIC_KEY, "input inMemory source");
        this.subscriber = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object event) {
                if (fail.equals("true")) {
                    sourceEventListener.onEvent(event, new String[]{prop1});
                } else {
                    sourceEventListener.onEvent(event, new String[]{prop1, prop2});
                }
            }

            @Override
            public String getTopic() {
                return topic;
            }
        };
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        InMemoryBroker.subscribe(subscriber);
    }
}
