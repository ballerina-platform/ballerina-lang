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
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.concurrent.ExecutorService;


public abstract class Source implements Snapshotable {
    private static final Logger log = Logger.getLogger(Source.class);
    private SourceMapper mapper;
    private String elementId;
    private boolean tryConnect = false;

    public void init(OptionHolder transportOptionHolder, SourceMapper sourceMapper,
                     ConfigReader configReader, ExecutionPlanContext executionPlanContext) {
        this.mapper = sourceMapper;
        this.elementId = executionPlanContext.getElementIdGenerator().createNewId();
        init(sourceMapper, transportOptionHolder, configReader, executionPlanContext);
    }

    public abstract void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,ConfigReader
            configReader, ExecutionPlanContext executionPlanContext);

    public abstract void connect() throws ConnectionUnavailableException;

    public abstract void disconnect();

    public abstract void destroy();

    public abstract void pause();

    public abstract void resume();

    public void connectWithRetry(ExecutorService executorService) {
        tryConnect = true;
        try {
            connect();
        } catch (ConnectionUnavailableException | RuntimeException e) {
            log.error(e.getMessage(), e);
        }
        // TODO: 2/9/17 Implement exponential retry
    }

    public SourceMapper getMapper() {
        return mapper;
    }

    public void shutdown() {
        tryConnect = false;
        disconnect();
        destroy();
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
