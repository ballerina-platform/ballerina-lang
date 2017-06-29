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
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a Sink type. these let users to publish events according to
 * some type. this type can either be local, jms or ws (or any custom extension)
 */
public abstract class Sink implements SinkListener, Snapshotable {

    private static final Logger log = Logger.getLogger(Sink.class);
    private String type;
    private SinkMapper mapper;
    private boolean tryConnect = false;
    private String elementId;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private ThreadLocal<DynamicOptions> trpDynamicOptions;

    public final void init(StreamDefinition streamDefinition, String type, OptionHolder transportOptionHolder,
                           ConfigReader sinkConfigReader, SinkMapper sinkMapper, String mapType,
                           OptionHolder mapOptionHolder, String payload, ConfigReader mapperConfigReader,
                           SiddhiAppContext siddhiAppContext) {
        this.type = type;
        this.elementId = siddhiAppContext.getElementIdGenerator().createNewId();
        init(streamDefinition, transportOptionHolder, sinkConfigReader, siddhiAppContext);
        if (sinkMapper != null) {
            sinkMapper.init(streamDefinition, mapType, mapOptionHolder, payload, this,
                    mapperConfigReader, siddhiAppContext);
            this.mapper = sinkMapper;
        }

    }

    public final void initOnlyTransport(StreamDefinition streamDefinition, OptionHolder transportOptionHolder,
                                        ConfigReader sinkConfigReader, SiddhiAppContext siddhiAppContext) {
        this.elementId = siddhiAppContext.getElementIdGenerator().createNewId();
        init(streamDefinition, transportOptionHolder, sinkConfigReader, siddhiAppContext);
    }

    /**
     * Supported dynamic options by the transport
     *
     * @return the list of supported dynamic option keys
     */
    public abstract String[] getSupportedDynamicOptions();

    /**
     * Will be called for initialing the {@link Sink}
     *
     * @param outputStreamDefinition containing stream definition bind to the {@link Sink}
     * @param optionHolder           Option holder containing static and dynamic options related to the {@link Sink}
     * @param sinkConfigReader       this hold the {@link Sink} extensions configuration reader.
     * @param siddhiAppContext       {@link SiddhiAppContext} of the parent siddhi app.
     */
    protected abstract void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                                 ConfigReader sinkConfigReader, SiddhiAppContext siddhiAppContext);

    @Override
    public final void publish(Object payload) throws ConnectionUnavailableException {
        try {
            DynamicOptions dynamicOptions = trpDynamicOptions.get();
            trpDynamicOptions.remove();
            publish(payload, dynamicOptions);
        } finally {
            trpDynamicOptions.remove();
        }

    }

    /**
     * Sending events via output transport
     *
     * @param payload          payload of the event
     * @param transportOptions one of the event constructing the payload
     * @throws ConnectionUnavailableException throw when connections are unavailable.
     */
    public abstract void publish(Object payload, DynamicOptions transportOptions)
            throws ConnectionUnavailableException;


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

    public final String getType() {
        return type;
    }

    public final SinkMapper getMapper() {
        return mapper;
    }

    public void connectWithRetry(ExecutorService executorService) {
        tryConnect = true;
        try {
            connect();
            isConnected.set(true);
        } catch (ConnectionUnavailableException | RuntimeException e) {
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

    public boolean isConnected() {
        return isConnected.get();
    }

    public void shutdown() {
        tryConnect = false;
        isConnected.set(false);
        disconnect();
        destroy();
    }

    @Override
    public final String getElementId() {
        return elementId;
    }

    void setTrpDynamicOptions(ThreadLocal<DynamicOptions> trpDynamicOptions) {
        this.trpDynamicOptions = trpDynamicOptions;
    }
}
