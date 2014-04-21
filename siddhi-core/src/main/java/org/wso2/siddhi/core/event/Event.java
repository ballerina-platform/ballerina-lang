/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.event;

import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.event.remove.RemoveEvent;

import java.util.Arrays;

public abstract class Event implements StreamEvent, AtomicEvent {

    private String streamId;
    private long timeStamp;
    private Object[] data;

    public Event(String streamId, long timeStamp, Object[] data) {
        this.streamId = streamId;
        this.timeStamp = timeStamp;
        this.data = data;
    }

    public String getStreamId() {
        return streamId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Object[] getData() {
        return data;
    }

    public Object getData(int i) {
        return data[i];
    }

    @Override
    public String toString() {
        return "Event{" +
                "streamId='" + streamId + '\'' +
                ", timeStamp=" + timeStamp +
                ", data=" + (data == null ? null : Arrays.asList(data)) +
                ", type=" + ((this instanceof InStream) ? "new" : ((this instanceof RemoveEvent) ? "remove" : "other"))+
                '}';
    }

    @Override
    public Event[] toArray() {
        return new Event[]{this};
    }

    public Object getData0() {
        return data[0];
    }

    public Object getData1() {
        return data[1];
    }

    public Object getData2() {
        return data[2];
    }

    public Object getData3() {
        return data[3];
    }

    public Object getData4() {
        return data[4];
    }

    public Object getData5() {
        return data[5];
    }

    public Object getData6() {
        return data[6];
    }

    public Object getData7() {
        return data[7];
    }

    public Object getData8() {
        return data[8];
    }

    public Object getData9() {
        return data[9];
    }
}
