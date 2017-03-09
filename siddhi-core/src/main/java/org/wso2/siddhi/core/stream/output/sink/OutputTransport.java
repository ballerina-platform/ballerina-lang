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

package org.wso2.siddhi.core.stream.output.sink;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.concurrent.ExecutorService;

/**
 * This is a OutputTransport type. these let users to publish events according to
 * some type. this type can either be local, jms or ws (or any custom extension)
 */
public abstract class OutputTransport implements OutputTransportListener {

    private static final Logger log = Logger.getLogger(OutputTransport.class);
    private AbstractDefinition streamDefinition;
    private String type;
    private OptionHolder optionHolder;
    private OutputMapper mapper;
    private boolean tryConnect = false;

    public void init(StreamDefinition streamDefinition, String type, OptionHolder transportOptionHolder, OutputMapper outputMapper,
                     String mapType, OptionHolder mapOptionHolder, String payload) {
        this.type = type;
        this.optionHolder = transportOptionHolder;
        this.streamDefinition = streamDefinition;
        String[] publishGroupDeterminers = init(streamDefinition, transportOptionHolder);
        outputMapper.init(streamDefinition, mapType, mapOptionHolder, payload, publishGroupDeterminers, transportOptionHolder);
        this.mapper = outputMapper;
    }

    protected abstract String[] init(StreamDefinition streamDefinition, OptionHolder optionHolder);

    /**
     * Will be called to connect to the backend before events are published
     *
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    public abstract void connect() throws ConnectionUnavailableException;

    /**
     * Will be called after all publishing is done, or when ConnectionUnavailableException is thrown
     */
    public abstract void disconnect();

    /**
     * Will be called at the end to clean all the resources consumed
     */
    public abstract void destroy();

    public String getType() {
        return type;
    }

    public OutputMapper getMapper() {
        return mapper;
    }

    public void connectWithRetry(ExecutorService executorService) {
        tryConnect = true;
        try {
            connect();
        } catch (ConnectionUnavailableException e) {
            log.error(e.getMessage(), e);
        }
        //// TODO: 2/9/17 implement exponential retry connection
//        while (tryConnect && !connected) {
//            try {
//                connect();
//                connected = true;
//            } catch (ConnectionUnavailableException e) {
//                log.error(e.getMessage()+", Retrying in ",e);
//            }
//        }

    }

    public void shutdown() {
        tryConnect = false;
        disconnect();
        destroy();
    }
}
