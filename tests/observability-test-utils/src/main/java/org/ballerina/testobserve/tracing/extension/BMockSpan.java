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
 *
 */

package org.ballerina.testobserve.tracing.extension;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.runtime.observability.ObservabilityConstants.CHECKPOINT_EVENT_NAME;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_MODULE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_SRC_POSITION;

/**
 * Class that holds the mock spans of the tracing integration tests.
 */
public class BMockSpan {

    private String operationName;
    private String traceId;
    private String spanId;
    private String parentId;
    private Map<String, Object> tags;
    private List<LogEntry> events;

    public BMockSpan(String operationName, String traceId, String spanId, String parentId, Map<String, Object> tags,
                     List<LogEntry> events) {
        this.operationName = operationName;
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.tags = tags;
        this.events = events;
    }

    public BMockSpan(String operationName, String traceId, String spanId, String parentId, Attributes attributes,
                     List<EventData> eventDataList) {
        this.operationName = operationName;
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.tags = new HashMap<>();
        attributes.forEach((attributeKey, o) -> tags.put(attributeKey.getKey(), o));
        this.events = new ArrayList<>();
        for (EventData eventData : eventDataList) {
            HashMap<String, HashMap<String, Object>> fields = new HashMap<>();
            HashMap<String, Object> field = new HashMap<>();
            eventData.getAttributes().forEach((attributeKey, o) -> field.put(attributeKey.getKey(), o));
            fields.put(eventData.getName(), field);
            this.events.add(new LogEntry(eventData.getEpochNanos(), fields));
        }
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public List<LogEntry> getEvents() {
        return events;
    }

    public List<BMockSpanEvent> getCheckpoints() {
        List<BMockSpanEvent> checkpoints;
        if (getEvents() != null) {
            checkpoints = new ArrayList<>(getEvents().size());
            for (LogEntry eventLog : getEvents()) {
                BMockSpan.BMockSpanEvent checkpoint = new BMockSpan.BMockSpanEvent(
                        (((Map) eventLog.fields().get(CHECKPOINT_EVENT_NAME)).
                                get(TAG_KEY_SRC_MODULE)).toString(),
                        (((Map) eventLog.fields().get(CHECKPOINT_EVENT_NAME)).
                                get(TAG_KEY_SRC_POSITION)).toString()
                );
                checkpoints.add(checkpoint);
            }
        } else {
            checkpoints = null;
        }
        return checkpoints;
    }

    /**
     * Trace Span Event.
     */
    public static class BMockSpanEvent {
        private String moduleID;
        private String positionID;

        public BMockSpanEvent(String moduleID, String positionID) {
            this.moduleID = moduleID;
            this.positionID = positionID;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            BMockSpanEvent that = (BMockSpanEvent) object;
            return Objects.equals(moduleID, that.moduleID) &&
                    Objects.equals(positionID, that.positionID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(moduleID, positionID);
        }
    }

    /**
     * This holds mock events.
     * Copied from opentracing mock span.
     */
    public static final class LogEntry {
        private final long timestampMicros;
        private final Map<String, ?> fields;

        public LogEntry(long timestampMicros, Map<String, ?> fields) {
            this.timestampMicros = timestampMicros;
            this.fields = fields;
        }

        public long timestampMicros() {
            return timestampMicros;
        }

        public Map<String, ?> fields() {
            return fields;
        }
    }
}
