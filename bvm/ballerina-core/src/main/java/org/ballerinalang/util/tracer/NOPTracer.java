/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.tracer;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;

import java.util.Collections;
import java.util.Map;

/**
 * {@link NOPTracer} will is a {@link Tracer} implementation
 * which performs no operations. This will be used when tracing is disabled.
 *
 * @since 0.964.1
 */
public class NOPTracer implements Tracer {
    @Override
    public void startSpan() {
        //do noting.
    }

    @Override
    public void finishSpan() {
        //do noting.
    }

    @Override
    public void log(Map<String, Object> fields) {
        //do noting.
    }

    @Override
    public void logError(Map<String, Object> fields) {
        //do noting.
    }

    @Override
    public void addTags(Map<String, String> tags) {
        //do noting.
    }

    @Override
    public String getConnectorName() {
        return TraceConstants.DEFAULT_CONNECTOR_NAME;
    }

    @Override
    public void setConnectorName(String connectorName) {
        //do noting.
    }

    @Override
    public String getActionName() {
        return TraceConstants.DEFAULT_ACTION_NAME;
    }

    @Override
    public void setActionName(String actionName) {
        //do noting.
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public void addProperty(String key, String value) {
        //do noting.
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return Collections.emptyMap();
    }

    @Override
    public String getInvocationID() {
        return null;
    }

    @Override
    public void setInvocationID(String invocationId) {
        //do noting.
    }

    @Override
    public void setExecutionContext(WorkerExecutionContext executionContext) {
        //do noting.
    }

    @Override
    public Map getSpans() {
        return Collections.emptyMap();
    }

    @Override
    public void setSpans(Map<String, ?> spans) {
        //do noting.
    }

    @Override
    public void generateInvocationID() {
        //do noting.
    }
}
