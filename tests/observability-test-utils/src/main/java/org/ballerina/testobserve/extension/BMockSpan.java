/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerina.testobserve.extension;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CHECKPOINT_EVENT_NAME;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_SRC_MODULE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_SRC_POSITION;

/**
 * Class that holds the mock spans of the tracing integration tests.
 */
public class BMockSpan {

    private String operationName;
    private String traceId;
    private String spanId;
    private String parentId;
    private Map<String, Object> tags;
    private List<BMockEvent> events;

    public BMockSpan(String operationName, String traceId, String spanId, String parentId, Map<String, Object> tags,
                     List<BMockEvent> events) {
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
            HashMap<String, Object> tags = new HashMap<>();
            eventData.getAttributes().forEach((attributeKey, o) -> tags.put(attributeKey.getKey(), o));
            this.events.add(new BMockEvent(eventData.getName(), eventData.getEpochNanos(), tags));
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

    public List<BMockEvent> getEvents() {
        return events;
    }

    public List<CheckPoint> getCheckpoints() {
        List<CheckPoint> checkpoints = null;
        if (getEvents() != null) {
            checkpoints = new ArrayList<>(getEvents().size());
            for (BMockEvent mockEvent : getEvents()) {
                if (mockEvent.getName().equals(CHECKPOINT_EVENT_NAME)) {
                    BMockSpan.CheckPoint checkpoint = new BMockSpan.CheckPoint(
                            mockEvent.getTags().get(TAG_KEY_SRC_MODULE).toString(),
                            mockEvent.getTags().get(TAG_KEY_SRC_POSITION).toString());
                    checkpoints.add(checkpoint);
                }
            }
        }
        return checkpoints;
    }

    /**
     * Control flow checkpoint.
     */
    public static class CheckPoint {
        private final String moduleID;
        private final String positionID;

        public CheckPoint(String moduleID, String positionID) {
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
            CheckPoint that = (CheckPoint) object;
            return Objects.equals(moduleID, that.moduleID) &&
                    Objects.equals(positionID, that.positionID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(moduleID, positionID);
        }
    }

    /**
     * This holds mock event.
     */
    public static final class BMockEvent {
        private final String name;
        private final long timestampMicros;
        private final Map<String, ?> tags;

        public BMockEvent(String name, long timestampMicros, Map<String, ?> tags) {
            this.name = name;
            this.timestampMicros = timestampMicros;
            this.tags = tags;
        }

        public String getName() {
            return name;
        }

        public long getTimestampMicros() {
            return timestampMicros;
        }

        public Map<String, ?> getTags() {
            return tags;
        }
    }
}
