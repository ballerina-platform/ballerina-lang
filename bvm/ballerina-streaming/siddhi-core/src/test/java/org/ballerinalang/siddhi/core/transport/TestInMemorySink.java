/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.ballerinalang.siddhi.core.stream.output.sink.InMemorySink;

/**
 * Test in-memory sink implementation.
 */

@Extension(
        name = "testInMemory",
        namespace = "sink",
        description = "In-memory sink for testing distributed sink in multi client mode. This dummy " +
                "sink simply overrides getSupportedDynamicOptions return nothing so that when distributed " +
                "sink will identify it as a multi-client sink as there are no dynamic options",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Event will be delivered to all" +
                "the subscribers of the same topic"),
        examples = @Example(
                syntax = "@sink(type='testInMemory', @map(type='passThrough'),\n" +
                        "@distribution(strategy='roundRobin',\n" +
                        "@destination(topic = 'topic1'), \n" +
                        "@destination(topic = 'topic2')))\n" +
                        "define stream BarStream (symbol string, price float, volume long);",
                description = "In the following example BarStream uses testInMemory transport which emit the Siddhi" +
                        "events internally without using external transport and transformation."
        )
)
public class TestInMemorySink extends InMemorySink {
    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }
}
