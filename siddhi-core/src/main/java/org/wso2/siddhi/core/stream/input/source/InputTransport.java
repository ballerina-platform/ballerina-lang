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

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;

import java.util.Map;

public abstract class InputTransport implements EternalReferencedHolder {

    private ExecutionPlanContext executionPlanContext;

    void init(Map<String, String> transportOptions, InputCallback inputCallback, ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
    }

    public abstract void init(Map<String, String> transportOptions, InputCallback inputCallback);
//    void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException;

    public abstract void connect() throws ConnectionUnavailableException;

    public abstract void disconnect();

    public abstract void destroy();

    public abstract boolean isEventDuplicatedInCluster();

    public abstract boolean isPolling();

    @Override
    public void start() {
        try {
            connect();
        } catch (ConnectionUnavailableException e) {
//            executionPlanContext.getScheduledExecutorService().schedule()
            e.printStackTrace();//todo implement exponential back off retry
        }
    }

    @Override
    public void stop() {
        disconnect();
        destroy();

    }
}
