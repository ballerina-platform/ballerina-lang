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

package org.wso2.siddhi.core.event.stream.populater;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.Arrays;
import java.util.List;

import static org.wso2.siddhi.core.util.SiddhiConstants.STREAM_ATTRIBUTE_INDEX_IN_TYPE;
import static org.wso2.siddhi.core.util.SiddhiConstants.STREAM_ATTRIBUTE_TYPE_INDEX;
import static org.wso2.siddhi.core.util.SiddhiConstants.STREAM_EVENT_CHAIN_INDEX;
import static org.wso2.siddhi.core.util.SiddhiConstants.STREAM_EVENT_INDEX_IN_CHAIN;

/**
 * The populater class that populates StateEvents
 */
public class SelectiveComplexEventPopulater implements ComplexEventPopulater {

    private List<StreamMappingElement> streamMappingElements;       //List to hold information needed for population

    SelectiveComplexEventPopulater(List<StreamMappingElement> streamMappingElements) {
        this.streamMappingElements = streamMappingElements;
    }

    public void populateComplexEvent(ComplexEvent complexEvent, Object[] data) {
        for (StreamMappingElement mappingElement : streamMappingElements) {
            populateStreamEvent(complexEvent, data[mappingElement.getFromPosition()], mappingElement.getToPosition());
        }
    }

    private void populateStreamEvent(ComplexEvent complexEvent, Object data, int[] toPosition) {
        if (toPosition == null) {
            // This happens when this data is not used by the query.
            return;
        }
        StreamEvent streamEvent;
        if (complexEvent instanceof StreamEvent) {
            streamEvent = (StreamEvent) complexEvent;
        } else {
            streamEvent = ((StateEvent) complexEvent).getStreamEvent(toPosition[STREAM_EVENT_CHAIN_INDEX]);
            for (int i = 0; i <= toPosition[STREAM_EVENT_INDEX_IN_CHAIN]; i++) {
                streamEvent = streamEvent.getNext();
            }
        }
        switch (toPosition[STREAM_ATTRIBUTE_TYPE_INDEX]) {
            case 0:
                streamEvent.setBeforeWindowData(data, toPosition[STREAM_ATTRIBUTE_INDEX_IN_TYPE]);
                break;
            case 1:
                streamEvent.setOnAfterWindowData(data, toPosition[STREAM_ATTRIBUTE_INDEX_IN_TYPE]);
                break;
            case 2:
                complexEvent.setOutputData(data, toPosition[STREAM_ATTRIBUTE_INDEX_IN_TYPE]);
                break;
            default:
                //will not happen
                throw new IllegalStateException("To Position cannot be :" + Arrays.toString(toPosition));
        }
    }

}
