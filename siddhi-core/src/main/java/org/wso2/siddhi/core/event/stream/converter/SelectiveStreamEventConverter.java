/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.event.stream.converter;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;

import java.util.List;

/**
 * The converter class that converts event into StreamEvent
 */
public class SelectiveStreamEventConverter implements EventConverter {

    private List<ConversionElement> conversionElements;       //List to hold information needed for conversion
    private StreamEventPool streamEventPool;


    public SelectiveStreamEventConverter(StreamEventPool streamEventPool, List<ConversionElement> conversionElements) {
        this.streamEventPool = streamEventPool;
        this.conversionElements = conversionElements;
    }

    private void convertToInnerStreamEvent(Object[] data, boolean isExpected, long timestamp, StreamEvent borrowedEvent) {
        for (ConversionElement conversionElement : conversionElements) {
            int[] position = conversionElement.getToPosition();
            switch (position[0]) {
                case 0:
                    borrowedEvent.setBeforeWindowData(data[conversionElement.getFromPosition()], position[1]);
                    break;
                case 1:
                    borrowedEvent.setOnAfterWindowData(data[conversionElement.getFromPosition()], position[1]);
                    break;
                case 2:
                    borrowedEvent.setOutputData(data[conversionElement.getFromPosition()], position[1]);
                    break;
                default:
                    //can not happen
            }
        }

        borrowedEvent.setExpired(isExpected);
        borrowedEvent.setTimestamp(timestamp);
    }


    public void convertEvent(Event event, StreamEvent borrowedEvent) {
        convertToInnerStreamEvent(event.getData(), event.isExpired(), event.getTimestamp(), borrowedEvent);
    }

    public void convertStreamEvent(StreamEvent streamEvent, StreamEvent borrowedEvent) {
        convertToInnerStreamEvent(streamEvent.getOutputData(), streamEvent.isExpired(), streamEvent.getTimestamp(), borrowedEvent);
    }

    @Override
    public void convertData(long timeStamp, Object[] data, StreamEvent borrowedEvent) {
        convertToInnerStreamEvent(data, false, timeStamp, borrowedEvent);
    }

    /**
     * Borrow a event from the pool
     */
    @Override
    public StreamEvent borrowEvent() {
        return streamEventPool.borrowEvent();
    }

    @Override
    public void returnEvent(StreamEvent streamEvent) {
        streamEventPool.returnEvent(streamEvent);
    }
}
