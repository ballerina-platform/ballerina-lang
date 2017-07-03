/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.List;

/**
 * Sink Callback is used to get {@link Event}s from Siddhi Streams using
 * {@link org.wso2.siddhi.core.stream.StreamJunction} and send them to {@link SinkMapper}s to do the mapping.
 */
public class SinkCallback extends StreamCallback {
    private static final Logger log = Logger.getLogger(SinkCallback.class);
    private AbstractDefinition outputStreamDefinition;
    private List<Sink> sinks;

    public SinkCallback(List<Sink> sinks, AbstractDefinition outputStreamDefinition) {
        this.sinks = sinks;
        this.outputStreamDefinition = outputStreamDefinition;
    }

    public void init(SiddhiAppContext siddhiAppContext) {
        // there's nothing to be done, since we moved the
        // type validation mechanism to the transport itself.
    }

    @Override
    public void receive(Event event) {
        if (event != null) {
            for (Sink sink : sinks) {
                sink.getMapper().mapAndSend(event);
            }
        }
    }

    @Override
    public void receive(Event[] events) {
        if (events != null) {
            for (Sink sink : sinks) {
                sink.getMapper().mapAndSend(events);
            }
        }
    }
}
