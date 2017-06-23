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

package org.wso2.siddhi.extension.output.transport.test;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.TestConnectionNotSupportedException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Extension(
        name = "test",
        namespace = "sink",
        description = ""
)
public class TestSink extends Sink {
    private static final Logger log = Logger.getLogger(TestSink.class);
    private static final String TOPIC_KEY = "topic";

    @Override
    public void init(String type, Map<String, String> options, Map<String, String> unmappedDynamicOptions) {
        List<String> availableConfigs = new ArrayList<>();
        availableConfigs.addAll(options.keySet());
        availableConfigs.addAll(unmappedDynamicOptions.keySet());
        if (!availableConfigs.contains(TOPIC_KEY)) {
            throw new SiddhiAppCreationException(String.format("{{%s}} configuration " +
                    "could not be found in provided configs.", TOPIC_KEY));
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException {
        log.info("TestSink:testConnect()");
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        log.info("TestSink:connect()");
    }

    @Override
    public void publish(Object event, Map<String, String> dynamicTransportOptions)
            throws ConnectionUnavailableException {
        log.info("TestSink:publish() | dynamicSinkOptions : " +
                dynamicTransportOptions.toString() + " | event : " + event.toString());
        InMemoryBroker.publish(dynamicTransportOptions.get(TOPIC_KEY), event);
    }

    @Override
    public void disconnect() {
        log.info("TestSink:disconnect()");
    }

    @Override
    public void destroy() {
        log.info("TestSink:destroy()");
    }

    @Override
    public boolean isPolled() {
        return false;
    }

}