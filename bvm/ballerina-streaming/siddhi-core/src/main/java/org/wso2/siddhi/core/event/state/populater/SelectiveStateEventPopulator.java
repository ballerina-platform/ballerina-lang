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
package org.wso2.siddhi.core.event.state.populater;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.state.StateEvent;

import java.util.List;

/**
 * The populater class that populates StateEvents
 */
public class SelectiveStateEventPopulator implements StateEventPopulator {

    private List<StateMappingElement> stateMappingElements;       //List to hold information needed for population

    public SelectiveStateEventPopulator(List<StateMappingElement> stateMappingElements) {
        this.stateMappingElements = stateMappingElements;
    }

    public void populateStateEvent(ComplexEvent complexEvent) {
        StateEvent stateEvent = (StateEvent) complexEvent;
        for (StateMappingElement stateMappingElement : stateMappingElements) {
            int toPosition = stateMappingElement.getToPosition();
            stateEvent.setOutputData(getFromData(stateEvent, stateMappingElement.getFromPosition()),
                    toPosition);
//            switch (toPosition[0]) {
//                case 0:
//                    stateEvent.setPreOutputData(getFromData(stateEvent, stateMappingElement.getFromPosition()),
//                            toPosition[1]);
//                    break;
//                case 1:
//                    stateEvent.setOutputData(getFromData(stateEvent, stateMappingElement.getFromPosition()),
//                            toPosition[1]);
//                    break;
//                default:
//                    //will not happen
//                    throw new IllegalStateException("To Position cannot be :" + toPosition[0]);
//            }
        }
    }

    private Object getFromData(StateEvent stateEvent, int[] fromPosition) {
        return stateEvent.getAttribute(fromPosition);
//        StreamEvent streamEvent = stateEvent.getStreamEvent(fromPosition[0]);
//        if (streamEvent == null) {
//            return null;
//        }
//        if (fromPosition[1] > 0) {
//            for (int i = 0, size = fromPosition[1]; i < size; i++) {
//                streamEvent = streamEvent.getNext();
//            }
//        }
//        switch (fromPosition[2]) {
//            case 0:
//                return streamEvent.getBeforeWindowData()[fromPosition[3]];
//            case 1:
//                return streamEvent.getOnAfterWindowData()[fromPosition[3]];
//            case 2:
//                return streamEvent.getEventTime()[fromPosition[3]];
//            default:
//                //will not happen
//                throw new IllegalStateException("3rd element in from position cannot be :" + fromPosition[2]);
//        }
    }

}
