/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.input.transport.tcp;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;

import java.util.Map;

@Extension(
        name = "tcp",
        namespace = "inputtransport",
        description = ""
)
public class TCPInputTransport extends InputTransport {
    static String CONTEXT = "context";
    static String QUEUE_SIZE = "queueSize";

    private SourceEventListener sourceEventListener;
    private String context;
    private StreamDefinition streamDefinition;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder, ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        context = optionHolder.validateAndGetStaticValue(CONTEXT,
                executionPlanContext.getName() + "/" + sourceEventListener.getStreamDefinition().getId());
        streamDefinition = StreamDefinition.id(context);
        streamDefinition.getAttributeList().addAll(sourceEventListener.getStreamDefinition().getAttributeList());
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        TCPServer.getInstance().start();
        TCPServer.getInstance().addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                sourceEventListener.onEvent(event);
            }

            @Override
            public void onEvents(Event[] events) {
                sourceEventListener.onEvent(events);
            }
        });
    }

    @Override
    public void disconnect() {
        TCPServer.getInstance().removeStreamListener(streamDefinition.getId());
    }

    @Override
    public void destroy() {
        TCPServer.getInstance().stop();
    }

    @Override
    public void pause() {
        TCPServer.getInstance().isPaused(true);
    }

    @Override
    public void resume() {
        TCPServer.getInstance().isPaused(false);
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //no state
    }
}