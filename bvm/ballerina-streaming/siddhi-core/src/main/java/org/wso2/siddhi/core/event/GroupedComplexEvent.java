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

package org.wso2.siddhi.core.event;

/**
 * Implementation of {@link ComplexEvent} to hold events belonging to the same GroupBy group.
 */
public class GroupedComplexEvent implements ComplexEvent {

    private static final long serialVersionUID = 3654677405648232168L;
    private final ComplexEvent complexEvent;
    private String groupKey;
    private ComplexEvent next;

    public GroupedComplexEvent(String groupKey, ComplexEvent complexEvent) {
        this.groupKey = groupKey;
        this.complexEvent = complexEvent;
    }

    @Override
    public ComplexEvent getNext() {
        return next;
    }

    @Override
    public void setNext(ComplexEvent events) {
        next = events;
    }

    @Override
    public Object[] getOutputData() {
        return complexEvent.getOutputData();
    }

    @Override
    public void setOutputData(Object object, int index) {
        complexEvent.setOutputData(object, index);
    }

    @Override
    public long getTimestamp() {
        return complexEvent.getTimestamp();
    }

    @Override
    public Object getAttribute(int[] position) {
        return complexEvent.getAttribute(position);
    }

    @Override
    public void setAttribute(Object object, int[] position) {
        complexEvent.setAttribute(object, position);
    }

    @Override
    public Type getType() {
        return complexEvent.getType();
    }

    @Override
    public void setType(Type type) {
        complexEvent.setType(type);
    }

    public ComplexEvent getComplexEvent() {
        return complexEvent;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
