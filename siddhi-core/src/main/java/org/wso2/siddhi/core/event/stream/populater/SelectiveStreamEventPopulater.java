/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.event.stream.populater;

import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.List;

/**
 * The populater class that populates StateEvents
 */
public class SelectiveStreamEventPopulater implements StreamEventPopulater {

    private List<StreamMappingElement> streamMappingElements;       //List to hold information needed for population

    public SelectiveStreamEventPopulater(List<StreamMappingElement> streamMappingElements) {
        this.streamMappingElements = streamMappingElements;
    }

    public void populateStreamEvent(StreamEvent streamEvent,Object[] data) {
        for (int i = 0, mappingElementsSize = streamMappingElements.size(); i < mappingElementsSize; i++) {
            populateStreamEvent(streamEvent, data[i], i);
        }
    }

    public void populateStreamEvent(StreamEvent streamEvent, Object data, int outputAttributeIndex) {
        StreamMappingElement streamMappingElement = streamMappingElements.get(outputAttributeIndex);
        int[] toPosition = streamMappingElement.getToPosition();
        switch (toPosition[0]) {
            case 0:
                streamEvent.setBeforeWindowData(data, streamMappingElement.getFromPosition());
                break;
            case 1:
                streamEvent.setOnAfterWindowData(data, streamMappingElement.getFromPosition());
                break;
            case 2:
                streamEvent.setOutputData(data, streamMappingElement.getFromPosition());
                break;
            default:
                //will not happen
                throw new IllegalStateException("To Position cannot be :" + toPosition[0]);
        }
    }

}
