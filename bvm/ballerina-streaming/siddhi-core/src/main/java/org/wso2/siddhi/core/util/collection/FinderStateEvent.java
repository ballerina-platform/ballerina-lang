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

/**
 * State Event holder used by {@link org.wso2.siddhi.core.executor.condition.InConditionExpressionExecutor}
 */
public class FinderStateEvent extends StateEvent {

    private static final long serialVersionUID = -6442877320125223084L;

    public FinderStateEvent(int streamEventsSize, int outputSize) {
        super(streamEventsSize, outputSize);
    }

    public void setEvent(StateEvent matchingStateEvent) {
        if (matchingStateEvent != null) {
            System.arraycopy(matchingStateEvent.getStreamEvents(), 0, streamEvents, 0,
                    matchingStateEvent.getStreamEvents().length);
        } else {
            for (int i = 0; i < streamEvents.length - 1; i++) {
                streamEvents[i] = null;
            }
        }
    }
}
