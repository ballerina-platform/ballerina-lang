/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.execution.query.input.stream;

import org.wso2.siddhi.query.api.execution.query.input.state.CountStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.NextStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StateElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StateInputStream extends InputStream {

    public enum Type {
        PATTERN, SEQUENCE
    }

    private Type stateType;
    private StateElement stateElement;
    private List<String> streamIdList;

    public StateInputStream(Type stateType, StateElement stateElement) {
        this.stateType = stateType;
        this.stateElement = stateElement;
        this.streamIdList = new ArrayList<String>(collectStreamIds(stateElement, new HashSet<String>()));
    }

    public StateElement getStateElement() {
        return stateElement;
    }

    public Type getStateType() {
        return stateType;
    }

    @Override
    public List<String> getStreamIds() {
        return streamIdList;
    }

    private HashSet<String> collectStreamIds(StateElement stateElement,
                                             HashSet<String> streamIds) {
        if (stateElement instanceof StateInputStream) {
            streamIds.addAll(((StateInputStream) stateElement).getStreamIds());
        } else if (stateElement instanceof BasicSingleInputStream) {
            streamIds.addAll(((BasicSingleInputStream) stateElement).getStreamIds());
        } else if (stateElement instanceof LogicalStateElement) {
            collectStreamIds(((LogicalStateElement) stateElement).getStreamStateElement1(), streamIds);
            collectStreamIds(((LogicalStateElement) stateElement).getStreamStateElement2(), streamIds);
        } else if (stateElement instanceof CountStateElement) {
            collectStreamIds(((CountStateElement) stateElement).getStreamStateElement(), streamIds);
        } else if (stateElement instanceof NextStateElement) {
            collectStreamIds(((NextStateElement) stateElement).getStateElement(), streamIds);
            collectStreamIds(((NextStateElement) stateElement).getNextStateElement(), streamIds);
        }
        return streamIds;
    }

    @Override
    public String toString() {
        return "StateInputStream{" +
                "stateType=" + stateType +
                ", stateElement=" + stateElement +
                ", streamIdList=" + streamIdList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateInputStream)) return false;

        StateInputStream that = (StateInputStream) o;

        if (stateElement != null ? !stateElement.equals(that.stateElement) : that.stateElement != null) return false;
        if (stateType != that.stateType) return false;
        if (streamIdList != null ? !streamIdList.equals(that.streamIdList) : that.streamIdList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stateType != null ? stateType.hashCode() : 0;
        result = 31 * result + (stateElement != null ? stateElement.hashCode() : 0);
        result = 31 * result + (streamIdList != null ? streamIdList.hashCode() : 0);
        return result;
    }
}
