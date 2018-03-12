/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.query.api.execution.query.output.stream;

import org.ballerinalang.siddhi.query.api.SiddhiElement;

/**
 * Query output stream.
 */
public abstract class OutputStream implements SiddhiElement {

    private static final long serialVersionUID = 1L;
    protected String id;
    protected OutputEventType outputEventType;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OutputEventType getOutputEventType() {
        return outputEventType;
    }

    public void setOutputEventType(OutputEventType outputEventType) {
        this.outputEventType = outputEventType;
    }

    @Override
    public String toString() {
        return "OutputStream{" +
                "outputEventType=" + outputEventType +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OutputStream)) {
            return false;
        }

        OutputStream that = (OutputStream) o;

        if (outputEventType != that.outputEventType) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = outputEventType != null ? outputEventType.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }

    /**
     * Output event types.
     */
    public enum OutputEventType {
        EXPIRED_EVENTS,
        CURRENT_EVENTS,
        ALL_EVENTS,
        ALL_RAW_EVENTS,
        EXPIRED_RAW_EVENTS
    }
}
