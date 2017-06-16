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
package org.wso2.siddhi.core.debugger;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SiddhiDebugger adds checkpoints, remove checkpoints and provide traversal functions like next and play.
 * The given operations are:
 * - next: Debug the next checkpoint
 * - play: Return to the same
 */
public class SiddhiDebugger {

    /**
     * Logger to log events.
     */
    private static final Logger log = Logger.getLogger(SiddhiDebugger.class);

    /**
     * Thread local flag to indicate whether the next endpoint must be blocked or not.
     */
    private static final ThreadLocal<Boolean> threadLocalNextFlag = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /**
     * Semaphore is used to pause the Siddhi thread at breakpoints.
     * The lock is acquired in {@link SiddhiDebugger#checkBreakPoint(String, QueryTerminal, ComplexEvent)} and
     * released in {@link SiddhiDebugger#next()} and {@link SiddhiDebugger#play()}.
     */
    private Semaphore breakPointLock = new Semaphore(0);

    /**
     * A map containing breakpoint name and the AtomicBoolean indicates whether the point has an active checkpoint.
     */
    private HashMap<String, AtomicBoolean> acquiredBreakPointsMap = new HashMap<String, AtomicBoolean>();

    /**
     * A flag to indicate whether the user called the next method or not.
     */
    private volatile AtomicBoolean enableNext = new AtomicBoolean(false);

    /**
     * Debug caller to send the {@link ComplexEvent} for debugging.
     */
    private SiddhiDebuggerCallback siddhiDebuggerCallback;

    /**
     * Snapshot service to retrieve the internal states of queries.
     */
    private SnapshotService snapshotService;

    /**
     * Create a new SiddhiDebugger instance for the given {@link SiddhiAppContext}.
     *
     * @param siddhiAppContext the SiddhiAppContext
     */
    public SiddhiDebugger(SiddhiAppContext siddhiAppContext) {
        this.snapshotService = siddhiAppContext.getSnapshotService();
    }

    /**
     * Acquire the given breakpoint.
     *
     * @param queryName     name of the Siddhi query
     * @param queryTerminal IN or OUT endpoint of the query
     */
    public void acquireBreakPoint(String queryName, QueryTerminal queryTerminal) {
        // TODO: Validate the query name
        String breakpointName = createBreakpointName(queryName, queryTerminal);
        AtomicBoolean breakPointLock = acquiredBreakPointsMap.get(breakpointName);
        if (breakPointLock == null) {
            breakPointLock = new AtomicBoolean(true);
            acquiredBreakPointsMap.put(breakpointName, breakPointLock);
        } else {
            breakPointLock.set(true);
        }
    }

    /**
     * Release the given breakpoint from the SiddhiDebugger.
     *
     * @param queryName     name of the Siddhi query
     * @param queryTerminal IN or OUT endpoint of the query
     */
    public void releaseBreakPoint(String queryName, QueryTerminal queryTerminal) {
        // TODO: Validate the query name
        acquiredBreakPointsMap.remove(createBreakpointName(queryName, queryTerminal));
    }

    /**
     * Release all the breakpoints from the Siddhi debugger. This may required to before stopping the debugger.
     */
    public void releaseAllBreakPoints() {
        acquiredBreakPointsMap.clear();
    }

    /**
     * Check for active breakpoint at the given endpoint and if there is an active checkpoint, block the thread and
     * send the event for debug callback.
     *
     * @param queryName     name of the Siddhi query
     * @param queryTerminal IN or OUT endpoint of the query
     * @param complexEvent  the {@link ComplexEvent} which is waiting at the endpoint
     */
    public void checkBreakPoint(String queryName, QueryTerminal queryTerminal, ComplexEvent complexEvent) {
        // Prevent other threads from calling this method simultaneously
        synchronized (this) {
            // Create the breakpoint name for the given query at the given endpoint
            String breakpointName = createBreakpointName(queryName, queryTerminal);

            // Get the breakpoint atomic boolean using the name
            AtomicBoolean breakpoint = acquiredBreakPointsMap.get(breakpointName);

            // Check whether this endpoint comes after the next call of another breakpoint
            boolean isNext = isNextEnabled();

            if (isNext) {
                // Reset next flag of this thread
                setNextEnabled(false);
            }

            if (breakpoint != null && breakpoint.get() || isNext) {
                // An active breakpoint is available or next method was called last time

                // Pass the debugger, query name, breakpoint and the events to the callback
                if (siddhiDebuggerCallback != null) {
                    siddhiDebuggerCallback.debugEvent(complexEvent, queryName, queryTerminal, this);
                }

                try {
                    // Lock the breakpoint so that the thread cannot progress further until it is released by next or
                    // play methods.
                    breakPointLock.acquire();

                    // After releasing the lock check how the lock was released. If it is by next, set the thread
                    // local flag to true.
                    if (this.enableNext.get()) {
                        // Must be assigned from the current thread. Do not move it to next or play methods
                        setNextEnabled(true);
                        this.enableNext.set(false);
                    }
                } catch (InterruptedException e) {
                    log.error("Error in acquiring breakpoint lock at " + breakpointName);
                }
            }
        }
    }

    /**
     * Release the current lock and wait for the events arrive to the next point. For this to work, the next endpoint
     * is not required to be a checkpoint marked by the user.
     * For example, if user adds breakpoint only for the IN of query 1, next will track the event in OUT of query 1.
     */
    public void next() {
        this.enableNext.set(true);
        this.breakPointLock.release();
    }

    /**
     * Release the current lock and wait for the next event arrive to the same break point.
     */
    public void play() {
        this.breakPointLock.release();
    }

    /**
     * A callback to be called by the Siddhi debugger when reaching an active breakpoint.
     *
     * @param siddhiDebuggerCallback the SiddhiDebuggerCallback
     */
    public void setDebuggerCallback(SiddhiDebuggerCallback siddhiDebuggerCallback) {
        synchronized (this) {
            this.siddhiDebuggerCallback = siddhiDebuggerCallback;
        }
    }

    /**
     * Get all the events stored in the {@link org.wso2.siddhi.core.util.snapshot.Snapshotable} entities of the given
     * query.
     *
     * @param queryName name of the Siddhi query
     * @return QueryState internal state of the query
     */
    public Map<String, Object> getQueryState(String queryName) {
        return this.snapshotService.queryState(queryName);
    }

    /**
     * Determine whether this checkpoint is enabled by the previous checkpoint.
     *
     * @return true of this is the next point otherwise false
     * @see SiddhiDebugger#setNextEnabled(boolean)
     */
    private boolean isNextEnabled() {
        return threadLocalNextFlag.get();
    }

    /**
     * Enable or disable checkpoint at the next end point.
     *
     * @param nextEnabled true or false indicating whether the next checkpoint is enabled or not
     * @see SiddhiDebugger#isNextEnabled()
     */
    private void setNextEnabled(boolean nextEnabled) {
        threadLocalNextFlag.set(nextEnabled);
    }

    /**
     * Create the breakpoint name for the given query at the given end point.
     *
     * @param queryName     the Siddhi query name
     * @param queryTerminal the IN or OUT end point of the query
     * @return the breakpoint name
     */
    private String createBreakpointName(String queryName, QueryTerminal queryTerminal) {
        return queryName + ":" + queryTerminal;
    }

    /**
     * SiddhiDebugger allows to add breakpoints only at the beginning and the end of a query.
     */
    public enum QueryTerminal {
        IN,
        OUT
    }
}
