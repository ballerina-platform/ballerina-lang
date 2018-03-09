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

package org.wso2.siddhi.core.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InMemorySource;

/**
 * Implementation of Source to receive events through in-memory transport for unavailability checking.
 */
@Extension(
        name = "testFailingInMemory",
        namespace = "source",
        description = "In-memory source for testing connection unavailable use-case",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Subscribes to sent on the "
                + "given topic."),
        examples = @Example(
                syntax = "@source(type='testFailingInMemory', @map(type='passThrough'),\n" +
                        "define stream BarStream (symbol string, price float, volume long)",
                description = "In this example BarStream uses inMemory transport which passes the received event " +
                        "internally without using external transport."
        )
)
public class TestFailingInMemorySource extends InMemorySource {
    private static final Logger LOG = LoggerFactory.getLogger(InMemorySource.class);
    public static int numberOfErrorOccurred;
    public static boolean fail = false;
    public static ConnectionCallback connectionCallback;

    public TestFailingInMemorySource() {
        numberOfErrorOccurred = 0;
        fail = false;
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        this.connectionCallback = connectionCallback;
        if (fail) {
            numberOfErrorOccurred++;
            throw new ConnectionUnavailableException("Connection failed!");
        }
        super.connect(connectionCallback);
    }
}
