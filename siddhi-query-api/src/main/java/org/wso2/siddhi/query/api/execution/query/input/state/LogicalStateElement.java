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
package org.wso2.siddhi.query.api.execution.query.input.state;

import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

/**
 * Logical state element used in pattern to handle logical operations
 */
public class LogicalStateElement implements StateElement {

    protected StreamStateElement streamStateElement1;
    protected Type type;
    protected StreamStateElement streamStateElement2;
    protected TimeConstant within;

    public LogicalStateElement(StreamStateElement streamStateElement1, Type type,
                               StreamStateElement streamStateElement2) {
        this.streamStateElement1 = streamStateElement1;
        this.type = type;
        this.streamStateElement2 = streamStateElement2;
    }

    public LogicalStateElement(StreamStateElement streamStateElement1, Type type, StreamStateElement
            streamStateElement2, TimeConstant within) {
        this.streamStateElement1 = streamStateElement1;
        this.type = type;
        this.streamStateElement2 = streamStateElement2;
        this.within = within;
    }

    public StreamStateElement getStreamStateElement1() {
        return streamStateElement1;
    }

    public StreamStateElement getStreamStateElement2() {
        return streamStateElement2;
    }

    public Type getType() {
        return type;
    }

    @Override
    public TimeConstant getWithin() {
        return within;
    }

    @Override
    public void setWithin(TimeConstant within) {
        this.within = within;
    }

    @Override
    public String toString() {
        return "LogicalStateElement{" +
                "streamStateElement1=" + streamStateElement1 +
                ", type=" + type +
                ", streamStateElement2=" + streamStateElement2 +
                ", within=" + within +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogicalStateElement)) {
            return false;
        }

        LogicalStateElement that = (LogicalStateElement) o;

        if (streamStateElement1 != null ? !streamStateElement1.equals(that.streamStateElement1) : that
                .streamStateElement1 != null) {
            return false;
        }
        if (streamStateElement2 != null ? !streamStateElement2.equals(that.streamStateElement2) : that
                .streamStateElement2 != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (within != null ? !within.equals(that.within) : that.within != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = streamStateElement1 != null ? streamStateElement1.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (streamStateElement2 != null ? streamStateElement2.hashCode() : 0);
        result = 31 * result + (within != null ? within.hashCode() : 0);
        return result;
    }

    /**
     * Different type of logical condition
     */
    public enum Type {
        AND,
        OR,
        NOT
    }
}
