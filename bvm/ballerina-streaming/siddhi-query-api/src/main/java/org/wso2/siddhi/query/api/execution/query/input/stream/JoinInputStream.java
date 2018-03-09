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
package org.wso2.siddhi.query.api.execution.query.input.stream;

import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Joining input streams in a query
 */
public class JoinInputStream extends InputStream {


    private final Within within;
    private final Expression per;
    private InputStream leftInputStream;
    private Type type;
    private InputStream rightInputStream;
    private Expression onCompare;
    private EventTrigger trigger;

    public JoinInputStream(SingleInputStream leftInputStream, Type type,
                           SingleInputStream rightInputStream, Expression onCompare,
                           EventTrigger trigger, Within within, Expression per) {
        this.leftInputStream = leftInputStream;
        this.type = type;
        this.rightInputStream = rightInputStream;
        this.onCompare = onCompare;
        this.trigger = trigger;
        this.within = within;
        this.per = per;
    }

    public InputStream getLeftInputStream() {
        return leftInputStream;
    }

    public Type getType() {
        return type;
    }

    public InputStream getRightInputStream() {
        return rightInputStream;
    }

    public Expression getOnCompare() {
        return onCompare;
    }

    public EventTrigger getTrigger() {
        return trigger;
    }

    public Within getWithin() {
        return within;
    }

    public Expression getPer() {
        return per;
    }

    @Override
    public List<String> getAllStreamIds() {
        List<String> list = new ArrayList<String>();
        for (String streamId : leftInputStream.getAllStreamIds()) {
            if (!list.contains(streamId)) {
                list.add(streamId);
            }
        }
        for (String streamId : rightInputStream.getAllStreamIds()) {
            if (!list.contains(streamId)) {
                list.add(streamId);
            }
        }
        return list;
    }

    @Override
    public List<String> getUniqueStreamIds() {
        List<String> list = new ArrayList<String>();
        for (String streamId : leftInputStream.getAllStreamIds()) {
            if (!list.contains(streamId)) {
                list.add(streamId);
            }
        }
        for (String streamId : rightInputStream.getAllStreamIds()) {
            if (!list.contains(streamId)) {
                list.add(streamId);
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JoinInputStream that = (JoinInputStream) o;

        if (leftInputStream != null ? !leftInputStream.equals(that.leftInputStream) : that.leftInputStream != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (rightInputStream != null ? !rightInputStream.equals(that.rightInputStream) :
                that.rightInputStream != null) {
            return false;
        }
        if (onCompare != null ? !onCompare.equals(that.onCompare) : that.onCompare != null) {
            return false;
        }
        if (trigger != that.trigger) {
            return false;
        }
        if (within != null ? !within.equals(that.within) : that.within != null) {
            return false;
        }
        return per != null ? per.equals(that.per) : that.per == null;
    }

    @Override
    public int hashCode() {
        int result = leftInputStream != null ? leftInputStream.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (rightInputStream != null ? rightInputStream.hashCode() : 0);
        result = 31 * result + (onCompare != null ? onCompare.hashCode() : 0);
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        result = 31 * result + (within != null ? within.hashCode() : 0);
        result = 31 * result + (per != null ? per.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JoinInputStream{" +
                "leftInputStream=" + leftInputStream +
                ", type=" + type +
                ", rightInputStream=" + rightInputStream +
                ", onCompare=" + onCompare +
                ", trigger=" + trigger +
                ", within=" + within +
                ", per=" + per +
                '}';
    }

    /**
     * Different join types
     */
    public enum Type {
        JOIN,
        INNER_JOIN,
        LEFT_OUTER_JOIN,
        RIGHT_OUTER_JOIN,
        FULL_OUTER_JOIN
    }

    /**
     * Different triggers to start joining process
     */
    public enum EventTrigger {
        LEFT,
        RIGHT,
        ALL
    }
}
