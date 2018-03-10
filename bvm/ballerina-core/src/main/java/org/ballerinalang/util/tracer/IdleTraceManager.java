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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link IdleTraceManager} is the idle implementation of {@link TraceManager}.
 * This will be used as a fallback when tracing is disabled.
 */
public class IdleTraceManager implements TraceManager {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Map<String, Object> startSpan(
            long invocationId, String spanName, Map<String, ?> spanContextMap,
            Map<String, String> tags, boolean makeActive, String serviceName) {
        return Collections.emptyMap();
    }

    @Override
    public void finishSpan(List<?> spans) {
        //do nothing.
    }

    @Override
    public void log(List<?> spans, Map<String, ?> fields) {
        //do nothing.
    }

    @Override
    public void addTags(List<?> spanList, Map<String, String> tags) {
        //do nothing.
    }

    @Override
    public Map<String, Object> extract(Object format, Map<String, String> headers,
                                       String serviceName) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> inject(Map<String, ?> activeSpanMap, Object format,
                                      String serviceName) {
        return Collections.emptyMap();
    }
}
