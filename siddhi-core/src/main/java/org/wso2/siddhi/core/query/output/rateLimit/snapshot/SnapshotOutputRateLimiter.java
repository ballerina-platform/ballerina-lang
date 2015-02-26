/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org)
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

package org.wso2.siddhi.core.query.output.rateLimit.snapshot;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;

public abstract class SnapshotOutputRateLimiter {
    static final Logger log = Logger.getLogger(SnapshotOutputRateLimiter.class);
    private WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter;
    protected StreamEventCloner streamEventCloner;
    protected StateEventCloner stateEventCloner;

    protected SnapshotOutputRateLimiter(WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        this.wrappedSnapshotOutputRateLimiter = wrappedSnapshotOutputRateLimiter;
    }

    public abstract void send(ComplexEventChunk complexEventChunk);

    public abstract void add(ComplexEvent complexEvent);

    public abstract SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter);

    public void setStreamEventCloner(StreamEventCloner streamEventCloner) {
        this.streamEventCloner = streamEventCloner;
    }

    public void setStateEventCloner(StateEventCloner stateEventCloner) {
        this.stateEventCloner = stateEventCloner;
    }

    protected void sendToCallBacks(ComplexEventChunk complexEventChunk) {
        wrappedSnapshotOutputRateLimiter.passToCallBacks(complexEventChunk);
    }

    public abstract void start();

    public abstract void stop();
}
