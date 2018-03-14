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

import java.util.Map;

/**
 * {@code Tracer} is the abstraction of the trace functionality.
 *
 * @since 0.964.1
 */
public interface Tracer {

    void startSpan();

    void finishSpan();

    void log(Map<String, Object> fields);

    void logError(Map<String, Object> fields);

    void addTags(Map<String, String> tags);

    String getConnectorName();

    void setConnectorName(String connectorName);

    String getActionName();

    void setActionName(String actionName);

    Map<String, String> getProperties();

    void addProperty(String key, String value);

    String getProperty(String key);

    Map<String, String> getTags();

    String getInvocationID();

    void setInvocationID(String invocationId);

    void setExecutionContext(WorkerExecutionContext executionContext);

    Map getSpans();

    void setSpans(Map<String, ?> spans);

    void generateInvocationID();

    boolean isRoot();
}
