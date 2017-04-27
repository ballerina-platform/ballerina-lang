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

package org.wso2.siddhi.extension.output.transport.tcp;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.OutputTransport;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.tcp.transport.TCPNettyClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Extension(
        name = "tcp",
        namespace = "outputtransport",
        description = ""
)
public class TCPOutputTransport extends OutputTransport {

    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String CONTEXT = "context";
    private static final Logger log = Logger.getLogger(TCPOutputTransport.class);
    private TCPNettyClient TCPNettyClient;
    private String host;
    private int port;
    private Option streamIdOption;
    private AtomicBoolean connected = new AtomicBoolean(false);

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{CONTEXT};
    }

    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder, ExecutionPlanContext executionPlanContext) {
        TCPNettyClient = new TCPNettyClient();
        host = optionHolder.validateAndGetStaticValue(HOST, "localhost");
        port = Integer.parseInt(optionHolder.validateAndGetStaticValue(PORT, "9892"));
        streamIdOption = optionHolder.validateAndGetOption(CONTEXT);
    }

    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        if (connected.compareAndSet(false, true)) {
            log.info("TCPOutputTransport:connect()");
            TCPNettyClient.connect(host, port);
        }
        String streamId = streamIdOption.getValue(dynamicOptions);
        if (payload instanceof Event) {
            TCPNettyClient.send(streamId, new Event[]{(Event) payload});
        } else {
            TCPNettyClient.send(streamId, (Event[]) payload);
        }
    }

    @Override
    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            if (TCPNettyClient != null) {
                log.info("TCPOutputTransport:disconnect()");
                TCPNettyClient.disconnect();
            }
        }
    }

    @Override
    public void destroy() {
        if (TCPNettyClient != null) {
            TCPNettyClient.shutdown();
            TCPNettyClient = null;
        }
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
