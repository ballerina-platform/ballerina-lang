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

package org.wso2.siddhi.core.util.collection;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

/**
 * Util mapper used by {@link org.wso2.siddhi.core.table.Table} callbacks to map Siddhi event to be inserted into Table.
 */
public class UpdateAttributeMapper {
    private final int updatingAttributePosition;
    private final int storeEventAttributePosition;
    private String storeEventAttributeName;
    private int matchingStreamEventPosition;

    public UpdateAttributeMapper(int updatingAttributePosition, int storeEventAttributePosition, String
            storeEventAttributeName, int matchingStreamEventPosition) {

        this.updatingAttributePosition = updatingAttributePosition;
        this.storeEventAttributePosition = storeEventAttributePosition;
        this.storeEventAttributeName = storeEventAttributeName;
        this.matchingStreamEventPosition = matchingStreamEventPosition;
    }

    public int getStoreEventAttributePosition() {
        return storeEventAttributePosition;
    }

    public Object getUpdateEventOutputData(StateEvent updatingEvent) {
        return updatingEvent.getStreamEvent(matchingStreamEventPosition).getOutputData()[updatingAttributePosition];
    }

    public void mapOutputData(StateEvent updatingEvent, StreamEvent storeEvent) {
        storeEvent.setOutputData(
                updatingEvent.getStreamEvent(matchingStreamEventPosition).getOutputData()[updatingAttributePosition],
                storeEventAttributePosition);
    }

    public String getStoreEventAttributeName() {
        return storeEventAttributeName;
    }
}

