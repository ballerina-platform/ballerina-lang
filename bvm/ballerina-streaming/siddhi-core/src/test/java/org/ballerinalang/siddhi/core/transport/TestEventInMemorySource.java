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
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.input.source.InMemorySource;

/**
 * Implementation of {@link org.ballerinalang.siddhi.core.stream.input.source.Source} to receive events
 * through in-memory transport.
 */
@Extension(
        name = "testEventInMemory",
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
public class TestEventInMemorySource extends InMemorySource {
    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{Event.class, Event[].class};
    }
}
