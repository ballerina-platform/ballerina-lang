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

import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.Reference;
import io.jaegertracing.spi.Reporter;
import io.opentracing.References;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClient;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClientHolder;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;
import org.ballerinalang.observe.trace.extension.choreo.model.ChoreoTraceSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Custom Jaeger tracing reporter for publishing stats to Choreo cloud.
 */
public class ChoreoJaegerReporter implements Reporter, AutoCloseable {
    private static final int PUBLISH_INTERVAL = 10 * 1000;
    private static final Logger LOGGER = LogFactory.getLogger();

    private Task task;

    public ChoreoJaegerReporter(int maxQueueSize) {
        ChoreoClient choreoClient = ChoreoClientHolder.getChoreoClient(this);
        if (Objects.isNull(choreoClient)) {
            throw new IllegalStateException("Choreo client is not initialized");
        }

        Timer time = new Timer("com.wso2.choreo.TraceSpanReporter-FlushTimer", true);
        task = new Task(choreoClient, maxQueueSize);
        time.schedule(task, PUBLISH_INTERVAL, PUBLISH_INTERVAL);
    }

    @Override
    public void report(JaegerSpan jaegerSpan) {
        task.append(jaegerSpan);
        task.flushIfFull();
    }

    @Override
    public void close() {
        LOGGER.info("sending all remaining traces to Choreo");
        task.flush();
    }

    /**
     * Worker which handles periodically publishing metrics to Choreo.
     */
    private static class Task extends TimerTask {
        private ChoreoClient choreoClient;
        private List<ChoreoTraceSpan> traceSpans;
        private int maxQueueSize;

        private Task(ChoreoClient choreoClient, int maxQueueSize) {
            this.choreoClient = choreoClient;
            this.traceSpans = new ArrayList<>();
            this.maxQueueSize = maxQueueSize;
        }

        @Override
        public void run() {
            this.flush();
        }

        private synchronized void append(JaegerSpan jaegerSpan) {
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
            traceSpans.add(traceSpan);
        }

        private void flush() {
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

        private void flushIfFull() {
            if (traceSpans.size() >= maxQueueSize) {
                flush();
            }
        }
    }
}
