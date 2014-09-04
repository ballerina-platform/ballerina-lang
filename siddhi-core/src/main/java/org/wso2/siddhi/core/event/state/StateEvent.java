/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.event.state;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.util.SiddhiConstants;

/**
 * Implementation of StateEvent to be used when executing join/pattern queries
 */
public class StateEvent implements ComplexEvent {
    private StreamEvent[] streamEvents;
    private StateEvent next;
    private int eventCount = 0;

    public StateEvent(int size){
        streamEvents = new StreamEvent[size];
    }

    public StateEvent(StreamEvent[] streamEvents){
        this.streamEvents = streamEvents.clone();
        eventCount = streamEvents.length;
    }

    public StreamEvent getEvent(int position){
        return streamEvents[position];
    }

    public void addEvent(StreamEvent streamEvent){
        streamEvents[eventCount] = streamEvent;
        eventCount++;
    }

    public void setNext(StateEvent stateEvent){
        next = stateEvent;
    }

    public StateEvent getNext() {
        return next;
    }

    /**
     *
     * @param position int array of 3 or 4 elements
     * int array of 3 : position[0]-which element of the streamEvents array, position[1]-BeforeWindowData or OutputData or AfterWindowData,
     *                  position[2]- which attribute
     * int array of 4 : position[0]-which element of the streamEvents array, position[1]-which event of the event chain,
     *                 position[3]- BeforeWindowData or OutputData or AfterWindowData, position[4]- which attribute
     * @return
     */
    @Override
    public Object getAttribute(int[] position){
        StreamEvent streamEvent = streamEvents[position[0]];

        if(position.length == 3){
            switch (position[1]){
                case -1:
                    return streamEvent.getBeforeWindowData()[position[2]];
                case 0:
                    return streamEvent.getOutputData()[position[2]];
                case 1:
                    return streamEvent.getOnAfterWindowData()[position[2]];
                default:
                    return null;
            }
        }

        for(int i=0;i<position[1];i++){
            streamEvent = streamEvent.getNext();
        }
        switch (position[2]){
            case SiddhiConstants.BEFORE_WINDOW_DATA_INDEX:
                return streamEvent.getBeforeWindowData()[position[3]];
            case SiddhiConstants.OUTPUT_DATA_INDEX:
                return streamEvent.getOutputData()[position[3]];
            case SiddhiConstants.AFTER_WINDOW_DATA_INDEX:
                return streamEvent.getOnAfterWindowData()[position[3]];
            default:
                return null;
        }


    }
}
