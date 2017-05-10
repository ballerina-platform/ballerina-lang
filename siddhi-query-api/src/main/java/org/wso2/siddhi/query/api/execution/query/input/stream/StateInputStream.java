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

import org.wso2.siddhi.query.api.execution.query.input.state.CountStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.EveryStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.NextStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StreamStateElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Input stream that handle states in query
 */
public class StateInputStream extends InputStream {

    private Type stateType;
    private StateElement stateElement;
    private List<String> streamIdList;

    public StateInputStream(Type stateType, StateElement stateElement) {
        this.stateType = stateType;
        this.stateElement = stateElement;
        this.streamIdList = collectStreamIds(stateElement, new ArrayList<String>());
    }

    public StateElement getStateElement() {
        return stateElement;
    }

    public Type getStateType() {
        return stateType;
    }

    @Override
    public List<String> getAllStreamIds() {
        return streamIdList;
    }

    @Override
    public List<String> getUniqueStreamIds() {
        List<String> uniqueStreams = new ArrayList<String>();
        for (String aStreamId : streamIdList) {
            if (!uniqueStreams.contains(aStreamId)) {
                uniqueStreams.add(aStreamId);
            }
        }
        return uniqueStreams;
    }

    private List<String> collectStreamIds(StateElement stateElement,
                                          List<String> streamIds) {
        if (stateElement instanceof LogicalStateElement) {
            collectStreamIds(((LogicalStateElement) stateElement).getStreamStateElement1(), streamIds);
            collectStreamIds(((LogicalStateElement) stateElement).getStreamStateElement2(), streamIds);
        } else if (stateElement instanceof CountStateElement) {
            collectStreamIds(((CountStateElement) stateElement).getStreamStateElement(), streamIds);
        } else if (stateElement instanceof EveryStateElement) {
            collectStreamIds(((EveryStateElement) stateElement).getStateElement(), streamIds);
        } else if (stateElement instanceof NextStateElement) {
            collectStreamIds(((NextStateElement) stateElement).getStateElement(), streamIds);
            collectStreamIds(((NextStateElement) stateElement).getNextStateElement(), streamIds);
        } else if (stateElement instanceof StreamStateElement) {
            BasicSingleInputStream basicSingleInputStream = ((StreamStateElement) stateElement)
                    .getBasicSingleInputStream();
            streamIds.add(basicSingleInputStream.getStreamId());
        }
        return streamIds;
    }

    public int getStreamCount(String streamId) {
        int count = 0;
        for (String aStreamId : streamIdList) {
            if (streamId.equals(aStreamId)) {
                count++;
            }
        }
        return count;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof StateInputStream)) {
            return false;
        }

        StateInputStream that = (StateInputStream) o;

        if (stateElement != null ? !stateElement.equals(that.stateElement) : that.stateElement != null) {
            return false;
        }
        if (stateType != that.stateType) {
            return false;
        }
        if (streamIdList != null ? !streamIdList.equals(that.streamIdList) : that.streamIdList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = stateType != null ? stateType.hashCode() : 0;
        result = 31 * result + (stateElement != null ? stateElement.hashCode() : 0);
        result = 31 * result + (streamIdList != null ? streamIdList.hashCode() : 0);
        return result;
    }

    /**
     * Different state management types
     */
    public enum Type {
        PATTERN,
        SEQUENCE
    }
}
