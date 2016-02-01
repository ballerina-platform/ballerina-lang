/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.Constant;

import java.util.ArrayList;
import java.util.List;

public class JoinInputStream extends InputStream {

    private InputStream leftInputStream;
    private Type type;
    private InputStream rightInputStream;
    private Expression onCompare;
    private EventTrigger trigger;
    private Constant within;

    public enum Type {JOIN, INNER_JOIN, LEFT_OUTER_JOIN, RIGHT_OUTER_JOIN, FULL_OUTER_JOIN}

    public enum EventTrigger {
        LEFT, RIGHT, ALL
    }

    public JoinInputStream(SingleInputStream leftInputStream, Type type,
                           SingleInputStream rightInputStream, Expression onCompare, Constant within,
                           EventTrigger trigger) {
        this.leftInputStream = leftInputStream;
        this.type = type;
        this.rightInputStream = rightInputStream;
        this.onCompare = onCompare;
        this.within = within;
        this.trigger = trigger;
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

    public Constant getWithin() {
        return within;
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


}
