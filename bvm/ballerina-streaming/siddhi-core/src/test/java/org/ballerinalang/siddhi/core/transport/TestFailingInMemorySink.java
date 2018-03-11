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
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.stream.output.sink.InMemorySink;
import org.ballerinalang.siddhi.core.util.transport.DynamicOptions;

@Extension(
        name = "testFailingInMemory",
        namespace = "sink",
        description = "In-memory sink for testing connection unavailable use-case",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Event will be delivered to all" +
                "the subscribers of the same topic"),
        examples = @Example(
                syntax = "@sink(type='inMemory', @map(type='passThrough'),\n" +
                        "define stream BarStream (symbol string, price float, volume long)",
                description = "In this example BarStream uses inMemory transport which emit the Siddhi " +
                        "events internally without using external transport and transformation."
        )
)
public class TestFailingInMemorySink extends InMemorySink {
    public static int numberOfErrorOccurred = 0;
    public static boolean fail;
    public static boolean failOnce;

    public TestFailingInMemorySink() {
        this.failOnce = false;
        this.fail = false;
        this.numberOfErrorOccurred = 0;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        if (fail || failOnce) {
            failOnce = false;
            numberOfErrorOccurred++;
            throw new ConnectionUnavailableException("Connection unavailable during connection");
        }
        super.connect();
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        if (fail || failOnce) {
            failOnce = false;
            numberOfErrorOccurred++;
            throw new ConnectionUnavailableException("Connection unavailable during publishing");
        }
        super.publish(payload, dynamicOptions);
    }
}
