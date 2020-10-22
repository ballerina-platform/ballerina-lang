/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.Reference;
import io.jaegertracing.spi.Reporter;
import io.opentracing.References;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClient;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClientHolder;
import org.ballerinalang.observe.trace.extension.choreo.client.error.ChoreoClientException;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;
import org.ballerinalang.observe.trace.extension.choreo.model.ChoreoTraceSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Custom Jaeger tracing reporter for publishing stats to Choreo cloud.
 *
 * @since 2.0.0
 */
public class ChoreoJaegerReporter implements Reporter, AutoCloseable {
    private static final int PUBLISH_INTERVAL_SECS = 10;
    private static final Logger LOGGER = LogFactory.getLogger();

    private final ScheduledExecutorService executorService;
    private final Task task;
    private final int maxQueueSize;

    public ChoreoJaegerReporter(int maxQueueSize) {
        ChoreoClient choreoClient;
        try {
            choreoClient = ChoreoClientHolder.getChoreoClient(this);
        } catch (ChoreoClientException e) {
            throw ErrorCreator.createError(
                    StringUtils
                            .fromString("Choreo client is not initialized. Please check Ballerina configurations."),
                    StringUtils.fromString(e.getMessage()));
        }
        if (Objects.isNull(choreoClient)) {
            throw new IllegalStateException("Choreo client is not initialized");
        }

        this.maxQueueSize = maxQueueSize;
        executorService = new ScheduledThreadPoolExecutor(1);
        task = new Task(choreoClient);
        executorService.scheduleAtFixedRate(task, PUBLISH_INTERVAL_SECS, PUBLISH_INTERVAL_SECS, TimeUnit.SECONDS);
        LOGGER.info("started publishing traces to Choreo");
    }

    @Override
    public void report(JaegerSpan jaegerSpan) {
        task.append(jaegerSpan);
        if (task.getSpanCount() >= maxQueueSize) {
            executorService.execute(task);
        }
    }

    @Override
    public void close() {
        LOGGER.info("sending all remaining traces to Choreo");
        executorService.execute(task);
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("failed to wait for publishing traces to complete due to " + e.getMessage());
        }
    }

    /**
     * Worker which handles periodically publishing metrics to Choreo.
     */
    private static class Task implements Runnable {
        private final ChoreoClient choreoClient;
        private final List<ChoreoTraceSpan> traceSpans;

        private Task(ChoreoClient choreoClient) {
            this.choreoClient = choreoClient;
            this.traceSpans = new ArrayList<>();
        }

        private void append(JaegerSpan jaegerSpan) {
            Map<String, String> tags = new HashMap<>();
            for (Map.Entry<String, Object> tagEntry : jaegerSpan.getTags().entrySet()) {
                tags.put(tagEntry.getKey(), tagEntry.getValue().toString());
            }
            List<ChoreoTraceSpan.Reference> references = new ArrayList<>(jaegerSpan.getReferences().size());
            for (Reference jaegerReference : jaegerSpan.getReferences()) {
                ChoreoTraceSpan.Reference reference = new ChoreoTraceSpan.Reference(
                        jaegerReference.getSpanContext().getTraceId(),
                        jaegerReference.getSpanContext().getSpanId(),
                        Objects.equals(jaegerReference.getType(), References.CHILD_OF)
                                ? ChoreoTraceSpan.Reference.Type.CHILD_OF
                                : ChoreoTraceSpan.Reference.Type.FOLLOWS_FROM
                );
                references.add(reference);
            }
            JaegerSpanContext spanContext = jaegerSpan.context();
            long timestamp = jaegerSpan.getStart() / 1000;  // Jaeger stores timestamp in microseconds by default
            long duration = jaegerSpan.getDuration() / 1000;    // Jaeger stores duration in microseconds by default
            ChoreoTraceSpan traceSpan = new ChoreoTraceSpan(spanContext.getTraceId(), spanContext.getSpanId(),
                    jaegerSpan.getServiceName(), jaegerSpan.getOperationName(), timestamp, duration, tags, references);
            synchronized (this) {
                traceSpans.add(traceSpan);
            }
        }

        @Override
        public void run() {
            ChoreoTraceSpan[] spansToBeSent;
            synchronized (this) {
                if (traceSpans.size() > 0) {
                    spansToBeSent = traceSpans.toArray(new ChoreoTraceSpan[0]);
                    traceSpans.clear();
                } else {
                    spansToBeSent = new ChoreoTraceSpan[0];
                }
            }
            if (spansToBeSent.length > 0) {
                if (!Objects.isNull(choreoClient)) {
                    try {
                        choreoClient.publishTraceSpans(spansToBeSent);
                    } catch (Throwable t) {
                        synchronized (this) {
                            traceSpans.addAll(Arrays.asList(spansToBeSent));
                        }
                        LOGGER.error("failed to publish traces to Choreo due to " + t.getMessage());
                    }
                }
            }
        }

        private int getSpanCount() {
            return traceSpans.size();
        }
    }
}
