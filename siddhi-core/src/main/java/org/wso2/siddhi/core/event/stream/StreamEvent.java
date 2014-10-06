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
package org.wso2.siddhi.core.event.stream;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.util.SiddhiConstants;

import java.util.Iterator;

/**
 * Standard processing event inside Siddhi. StreamEvent will be created
 * from StreamEvent before sending to relevant Queries.
 */
public class StreamEvent implements ComplexEvent {

    protected long timestamp = -1;
    protected Object[] outputData;              //Attributes to sent as output
    protected boolean isExpired = false;
    private Object[] beforeWindowData;          //Attributes before window execution
    private Object[] onAfterWindowData;         //Attributes on and after window execution
    private StreamEvent next;

    public StreamEvent(int beforeWindowDataSize, int onAfterWindowDataSize, int outputDataSize) {
        beforeWindowData = new Object[beforeWindowDataSize];
        onAfterWindowData = new Object[onAfterWindowDataSize];
        outputData = new Object[outputDataSize];
    }

    public StreamEvent() {
        //Do nothing
    }

    public Object[] getBeforeWindowData() {
        return beforeWindowData;
    }

    public void setBeforeWindowData(Object[] beforeWindowData) {
        this.beforeWindowData = beforeWindowData;
    }

    public Object[] getOnAfterWindowData() {
        return onAfterWindowData;
    }

    public void setOnAfterWindowData(Object[] onAfterWindowData) {
        this.onAfterWindowData = onAfterWindowData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Object[] getOutputData() {
        return outputData;
    }

    public void setOutputData(Object[] outputData) {
        this.outputData = outputData;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public StreamEvent getNext() {
        return next;
    }

    public void setNext(StreamEvent next) {
        this.next = next;
    }

    /**
     * @param position int array of 4 elements
     *                 position[0] and position[1] are discarded
     *                 position[2]-BeforeWindowData or OutputData or AfterWindowData, position[3]- which attribute
     * @return
     */
    @Override
    public Object getAttribute(int[] position) {
        StreamEvent streamEvent = this;
        switch (position[2]) {
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

    public void setOutputData(Object object, int index) {
        this.outputData[index] = object;
    }

    public void setOnAfterWindowData(Object object, int index) {
        this.onAfterWindowData[index] = object;
    }

    public void setBeforeWindowData(Object object, int index) {
        this.beforeWindowData[index] = object;
    }

    public int getOnAfterWindowDataSize() {
        return this.onAfterWindowData.length;
    }

    public int getOutputDataSize() {
        return this.outputData.length;
    }

    public StreamEventIterator getIterator(){
        StreamEventIterator iterator = new StreamEventIterator();
        iterator.setEvent(this);
        return iterator;
    }

    /**
     * Add a given event to the last position of the event chain
     *
     * @param event
     */
    public void addToLast(StreamEvent event) {
        if (next == null) {
            next = event;
        } else {
            next.addToLast(event);
        }
    }
}
