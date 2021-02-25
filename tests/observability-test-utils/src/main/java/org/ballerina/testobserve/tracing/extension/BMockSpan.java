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

import io.opentracing.mock.MockSpan;

import java.util.ArrayList;
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
    private long traceId;
    private long spanId;
    private long parentId;
    private Map<String, Object> tags;
    private List<MockSpan.LogEntry> events;

    public BMockSpan(String operationName, long traceId, long spanId, long parentId, Map<String, Object> tags,
                     List<MockSpan.LogEntry> events) {
        this.operationName = operationName;
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.tags = tags;
        this.events = events;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public List<MockSpan.LogEntry> getEvents() {
        return events;
    }

    public List<BMockSpanEvent> getCheckpoints() {
        List<BMockSpanEvent> checkpoints;
        if (getEvents() != null) {
            checkpoints = new ArrayList<>(getEvents().size());
            for (MockSpan.LogEntry eventLog : getEvents()) {
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
}
