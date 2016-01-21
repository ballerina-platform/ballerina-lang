/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.input.stream.state.receiver;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.input.SingleProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.state.StateStreamRuntime;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

public class SequenceSingleProcessStreamReceiver extends SingleProcessStreamReceiver {


    private StateStreamRuntime stateStreamRuntime;
    private final String lockKey;

    public SequenceSingleProcessStreamReceiver(String streamId, StateStreamRuntime stateStreamRuntime, String lockKey, LatencyTracker latencyTracker) {
        super(streamId, latencyTracker);
        this.stateStreamRuntime = stateStreamRuntime;
        this.lockKey = lockKey;
    }

    public void setStateStreamRuntime(StateStreamRuntime stateStreamRuntime) {
        this.stateStreamRuntime = stateStreamRuntime;
    }

    public SequenceSingleProcessStreamReceiver clone(String key) {
        return new SequenceSingleProcessStreamReceiver(streamId + key, null, key, latencyTracker);
    }

    protected void stabilizeStates() {
        stateStreamRuntime.resetAndUpdate();
    }

    @Override
    public void receive(ComplexEvent complexEvent) {
        synchronized (lockKey) {
            super.receive(complexEvent);
        }
    }

    @Override
    public void receive(Event event) {
        synchronized (lockKey) {
            super.receive(event);
        }
    }

    @Override
    public void receive(Event[] events) {
        synchronized (lockKey) {
            super.receive(events);
        }
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        synchronized (lockKey) {
            super.receive(event, endOfBatch);
        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        synchronized (lockKey) {
            super.receive(timeStamp, data);
        }
    }
}
