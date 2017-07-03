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

package org.wso2.siddhi.core.stream.input.source;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.transport.BackoffRetryCounter;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class to represent Event Sources. Events Sources are the object entry point to Siddhi from external
 * transports. Each source represent a transport type. Whenever Siddhi need to support a new transport, a new Event
 * source should be implemented.
 */
public abstract class Source implements Snapshotable {
    private static final Logger LOG = Logger.getLogger(Source.class);
    private String type;
    private SourceMapper mapper;
    private StreamDefinition streamDefinition;
    private String elementId;

    private AtomicBoolean isTryingToConnect = new AtomicBoolean(false);
    private BackoffRetryCounter backoffRetryCounter = new BackoffRetryCounter();
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private ScheduledExecutorService scheduledExecutorService;
    private ConnectionCallback connectionCallback = new ConnectionCallback();

    public final void init(String sourceType, OptionHolder transportOptionHolder, SourceMapper sourceMapper,
                           String[] transportPropertyNames, ConfigReader configReader,
                           StreamDefinition streamDefinition, SiddhiAppContext siddhiAppContext) {
        this.type = sourceType;
        this.mapper = sourceMapper;
        this.streamDefinition = streamDefinition;
        this.elementId = siddhiAppContext.getElementIdGenerator().createNewId();
        init(sourceMapper, transportOptionHolder, transportPropertyNames, configReader, siddhiAppContext);
        scheduledExecutorService = siddhiAppContext.getScheduledExecutorService();
    }

    public abstract void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                              String[] transportPropertyNames, ConfigReader configReader,
                              SiddhiAppContext siddhiAppContext);

    public abstract void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException;

    public abstract void disconnect();

    public abstract void destroy();

    public abstract void pause();

    public abstract void resume();

    public void connectWithRetry() {
        if (!isConnected.get()) {
            isTryingToConnect.set(true);
            try {
                connect(connectionCallback);
                isConnected.set(true);
                isTryingToConnect.set(false);
                backoffRetryCounter.reset();
            } catch (ConnectionUnavailableException | RuntimeException e) {
                LOG.error("Error while connecting at Source '" + type + "' at '" + streamDefinition.getId() +
                        "', " + e.getMessage() + ", will retry in '" +
                        backoffRetryCounter.getTimeInterval() + "'.", e);
                scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        connectWithRetry();
                    }
                }, backoffRetryCounter.getTimeIntervalMillis(), TimeUnit.MILLISECONDS);
                backoffRetryCounter.increment();
            }
        }
    }

    public final SourceMapper getMapper() {
        return mapper;
    }

    public void shutdown() {
        try {
            disconnect();
            destroy();
        } finally {
            isConnected.set(false);
            isTryingToConnect.set(false);
        }
    }

    @Override
    public final String getElementId() {
        return elementId;
    }

    public String getType() {
        return type;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    /**
     * Callback class used to pass connection exception during message retrieval
     */
    public class ConnectionCallback {
        public void onError(ConnectionUnavailableException e) {
            disconnect();
            isConnected.set(false);
            LOG.error("Connection unavailable at Sink '" + type + "' at '" + streamDefinition.getId() +
                    "', " + e.getMessage() + ", will retry connection immediately.", e);
            connectWithRetry();
        }
    }
}
