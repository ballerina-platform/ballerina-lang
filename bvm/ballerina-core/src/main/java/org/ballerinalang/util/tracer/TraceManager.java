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

import java.util.List;
import java.util.Map;

/**
 * {@link TraceManager} api acts as a bridge between ballerina core and the ballerina tracing modules.
 * This will remove OpenTracing dependencies from the ballerina core.
 *
 * @since 0.963.1
 */
public interface TraceManager {

    boolean isEnabled();

    /**
     * Starts a new spans for each loaded tracer.
     *
     * @param invocationId   to denote resource invocation.
     * @param spanName       to denote the name of the span.
     * @param spanContextMap of the parent span.
     * @param tags           to be included in the span.
     * @param makeActive     to mark the span as active.
     * @param serviceName    of the invoked resource.
     * @return {@link Map} of spans per tracer.
     */
    Map<String, Object> startSpan(long invocationId, String spanName, Map<String, ?> spanContextMap,
                             Map<String, String> tags, boolean makeActive, String serviceName);

    /**
     * Finishes the given list of spans.
     *
     * @param spans list to be finish.
     */
    void finishSpan(List<?> spans);

    /**
     * Logs provided fields in given list of spans.
     *
     * @param spans  to include the logs.
     * @param fields to log.
     */
    void log(List<?> spans, Map<String, ?> fields);

    /**
     * Adds tags to the given list of spans.
     *
     * @param spanList to add tags.
     * @param tags     to be added.
     */
    void addTags(List<?> spanList, Map<String, String> tags);

    /**
     * Extract span context from transport.
     *
     * @param format      of the headers.
     * @param headers     map.
     * @param serviceName of the invoked resource.
     * @return the {@link Map} of extracted context.
     */
    Map<String, Object> extract(Object format, Map<String, String> headers, String serviceName);

    /**
     * Returns the map of context to be injected into the transport.
     *
     * @param activeSpanMap of current spans per tracer (zipkin, jaeger, etc..)
     * @param format        of the headers.
     * @param serviceName   of the invoked resource.
     * @return the map of context to be injected into the transport.
     */
    Map<String, String> inject(Map<String, ?> activeSpanMap, Object format, String serviceName);

}
