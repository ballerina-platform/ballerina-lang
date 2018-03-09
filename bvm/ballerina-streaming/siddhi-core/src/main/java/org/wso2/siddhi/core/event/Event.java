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
package org.wso2.siddhi.core.event;

import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.Arrays;

/**
 * Event that is used external to Siddhi
 */
public class Event {

    protected long timestamp = -1;
    protected Object[] data;
    protected boolean isExpired = false;

    public Event(long timestamp, Object[] data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public Event() {
        data = new Object[0];
    }

    public Event(int dataSize) {
        this.data = new Object[dataSize];
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public Object getData(int i) {
        return data[i];
    }

    public boolean isExpired() {
        return isExpired;
    }

    @Override
    public String toString() {
        return "Event{" +
                "timestamp=" + timestamp +
                ", data=" + Arrays.toString(data) +
                ", isExpired=" + isExpired +
                '}';
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Event copyFrom(Event event) {
        timestamp = event.timestamp;
        System.arraycopy(event.data, 0, data, 0, data.length);
        isExpired = event.isExpired;
        return this;
    }

    public Event copyFrom(ComplexEvent complexEvent) {
        timestamp = complexEvent.getTimestamp();
        System.arraycopy(complexEvent.getOutputData(), 0, data, 0, data.length);
        isExpired = complexEvent.getType() == StreamEvent.Type.EXPIRED;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }

        Event event = (Event) o;

        if (isExpired != event.isExpired) {
            return false;
        }
        if (timestamp != event.timestamp) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(data, event.data);

    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + (isExpired ? 1 : 0);
        return result;
    }
}
