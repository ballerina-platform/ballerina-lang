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

package org.ballerinalang.siddhi.core.query.output.ratelimit.snapshot;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.state.StateEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.util.Schedulable;
import org.ballerinalang.siddhi.core.util.lock.LockWrapper;

import java.util.Map;

/**
 * Parent implementation to run the {@link org.ballerinalang.siddhi.core.util.Scheduler} to handle periodic
 * snapshot rate limiting.
 */
public abstract class SnapshotOutputRateLimiter implements Schedulable {
    protected SiddhiAppContext siddhiAppContext;
    protected StreamEventCloner streamEventCloner;
    protected StateEventCloner stateEventCloner;
    protected LockWrapper lockWrapper;
    private WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter;
    private boolean receiveStreamEvent;

    protected SnapshotOutputRateLimiter(WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter,
                                        SiddhiAppContext siddhiAppContext) {
        this.wrappedSnapshotOutputRateLimiter = wrappedSnapshotOutputRateLimiter;
        this.siddhiAppContext = siddhiAppContext;
    }

    public abstract void process(ComplexEventChunk complexEventChunk);

    public abstract SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter
            wrappedSnapshotOutputRateLimiter);

    public void setStreamEventCloner(StreamEventCloner streamEventCloner) {
        this.streamEventCloner = streamEventCloner;
        this.receiveStreamEvent = true;
    }

    public void setStateEventCloner(StateEventCloner stateEventCloner) {
        this.stateEventCloner = stateEventCloner;
        this.receiveStreamEvent = false;
    }

    protected void sendToCallBacks(ComplexEventChunk complexEventChunk) {
        wrappedSnapshotOutputRateLimiter.passToCallBacks(complexEventChunk);
    }

    /**
     * Clones a given complex event.
     *
     * @param complexEvent Complex event to be cloned
     * @return Cloned complex event
     */
    protected ComplexEvent cloneComplexEvent(ComplexEvent complexEvent) {
        if (receiveStreamEvent) {
            return streamEventCloner.copyStreamEvent((StreamEvent) complexEvent);
        } else {
            return stateEventCloner.copyStateEvent((StateEvent) complexEvent);
        }
    }

    public abstract void start();

    public abstract void stop();

    public abstract Map<String, Object> currentState();

    public abstract void restoreState(Map<String, Object> state);

    public void setQueryLock(LockWrapper lockWrapper) {
        this.lockWrapper = lockWrapper;
    }
}
