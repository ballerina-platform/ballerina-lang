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
package org.wso2.siddhi.core.event.stream.constructor;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;

import java.util.List;

public class SimpleStreamEventConstructor implements EventConstructor {
    private StreamEventPool streamEventPool;
    private List<ConverterElement> converterElements;

    public SimpleStreamEventConstructor(StreamEventPool streamEventPool, List<ConverterElement> converterElements) {
        this.streamEventPool = streamEventPool;
        this.converterElements = converterElements;
    }

    private StreamEvent convertToInnerStreamEvent(Object[] data, boolean isExpected, long timestamp) {
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        for (ConverterElement element : converterElements) {
            streamEvent.setOutputData(data[element.getFromPosition()], element.getToPosition()[1]);
        }
        streamEvent.setExpired(isExpected);
        streamEvent.setTimestamp(timestamp);

        return streamEvent;
    }

    public StreamEvent constructStreamEvent(Event event) {
        return convertToInnerStreamEvent(event.getData(), event.isExpired(), event.getTimestamp());
    }

    public StreamEvent constructStreamEvent(StreamEvent streamEvent) {
        return convertToInnerStreamEvent(streamEvent.getOutputData(), streamEvent.isExpired(), streamEvent.getTimestamp());
    }

    @Override
    public StreamEvent constructStreamEvent(long timeStamp, Object[] data) {
        return convertToInnerStreamEvent(data, false, timeStamp);
    }

    @Override
    public void returnEvent(StreamEvent streamEvent) {
        streamEventPool.returnEvent(streamEvent);
    }

}
