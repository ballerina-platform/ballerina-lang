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
import org.wso2.siddhi.tcp.transport.TcpNettyClient;

import java.util.Map;

@Extension(
        name = "tcp",
        namespace = "outputtransport",
        description = ""
)
public class TcpOutputTransport extends OutputTransport {

    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String TCP_STREAM_ID = "tcp.stream.id";
    private static final Logger log = Logger.getLogger(TcpOutputTransport.class);
    private TcpNettyClient tcpNettyClient;
    private String host;
    private int port;
    private Option streamIdOption;

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{TCP_STREAM_ID};
    }

    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder, ExecutionPlanContext executionPlanContext) {
        tcpNettyClient = new TcpNettyClient();
        host = optionHolder.validateAndGetStaticValue(HOST, "localhost");
        port = Integer.parseInt(optionHolder.validateAndGetStaticValue(PORT, "8080"));
        streamIdOption = optionHolder.getOrCreateOption(TCP_STREAM_ID, outputStreamDefinition.getId());
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        log.info("TcpOutputTransport:connect()");
        tcpNettyClient.connect(host, port);
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        String streamId = streamIdOption.getValue(dynamicOptions);
        tcpNettyClient.send(streamId, (Event[]) payload);
    }

    @Override
    public void disconnect() {
        if (tcpNettyClient != null) {
            tcpNettyClient.disconnect();
        }
    }

    @Override
    public void destroy() {
        if (tcpNettyClient != null) {
            tcpNettyClient.shutdown();
            tcpNettyClient = null;
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
