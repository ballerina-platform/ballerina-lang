/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.parser.SchedulerParser;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pre processor of not operator.
 */
public class AbsentStreamPreStateProcessor extends StreamPreStateProcessor implements AbsentPreStateProcessor {

    /**
     * Scheduler to trigger events after the waitingTime.
     */
    private Scheduler scheduler;

    /**
     * The time defined by 'for' in an absence pattern.
     */
    private long waitingTime = -1;

    /**
     * The timestamp of the last event received by this processor.
     */
    private long lastArrivalTime;

    /**
     * This flag turns to false after processing the first event if 'every' is not used.
     * This is used to process only one pattern if 'every' is not used.
     */
    private boolean active = true;

    /**
     * TimeConstant to be used by cloneProcessor method.
     */
    private TimeConstant waitingTimeConstant;


    /**
     * Construct an AbsentStreamPreStateProcessor object.
     *
     * @param stateType    PATTERN or SEQUENCE
     * @param withinStates the time defined by 'within' keyword
     * @param waitingTime  the waiting time defined by 'for' keyword
     */
    public AbsentStreamPreStateProcessor(StateInputStream.Type stateType, List<Map.Entry<Long, Set<Integer>>>
            withinStates, TimeConstant waitingTime) {
        super(stateType, withinStates);
        // Not operator always has 'for' time
        this.waitingTime = waitingTime.value();
        this.waitingTimeConstant = waitingTime;
    }

    @Override
    public void updateLastArrivalTime(long timestamp) {
        synchronized (this) {
            this.lastArrivalTime = timestamp;
        }
    }

    @Override
    public void addState(StateEvent stateEvent) {

        if (!this.active) {
            // 'every' keyword is not used and already a pattern is processed
            return;
        }
        synchronized (this) {
            if (stateType == StateInputStream.Type.SEQUENCE) {
                newAndEveryStateEventList.clear();
                newAndEveryStateEventList.add(stateEvent);
            } else {
                newAndEveryStateEventList.add(stateEvent);
            }
        }
        // If this is the first processor, nothing to receive from previous patterns
        if (!isStartState) {
            // Start the scheduler
            scheduler.notifyAt(stateEvent.getTimestamp() + waitingTime);
        }
    }

    @Override
    public void resetState() {

        // Clear the events added by the previous processor
        pendingStateEventList.clear();
        if (isStartState) {
            if (stateType == StateInputStream.Type.SEQUENCE &&
                    thisStatePostProcessor.nextEveryStatePerProcessor == null &&
                    !((StreamPreStateProcessor) thisStatePostProcessor.nextStatePerProcessor)
                            .pendingStateEventList.isEmpty()) {
                // Sequence without 'every' keyword and the next processor has pending events to be processed
                return;
            }
            // Start state needs a new event
            init();
        }
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

        if (!this.active) {
            // Every keyword is not used and already a pattern is processed
            return;
        }
        boolean notProcessed = true;
        long currentTime = complexEventChunk.getFirst().getTimestamp();
        if (currentTime >= this.lastArrivalTime + waitingTime) {
            synchronized (this) {
                // If the process method is called, it is guaranteed that the waitingTime is passed
                boolean initialize;
                initialize = isStartState && newAndEveryStateEventList.isEmpty() && pendingStateEventList.isEmpty();
                if (initialize && stateType == StateInputStream.Type.SEQUENCE &&
                        thisStatePostProcessor.nextEveryStatePerProcessor == null && this.lastArrivalTime > 0) {
                    // Sequence with no every but an event arrived
                    initialize = false;
                }

                if (initialize) {
                    // This is the first processor and no events received so far
                    StateEvent stateEvent = stateEventPool.borrowEvent();
                    addState(stateEvent);
                } else if (stateType == StateInputStream.Type.SEQUENCE && !newAndEveryStateEventList.isEmpty()) {
                    this.resetState();
                }

                this.updateState();

                ComplexEventChunk<StateEvent> retEventChunk = new ComplexEventChunk<>(false);

                Iterator<StateEvent> iterator = pendingStateEventList.iterator();
                while (iterator.hasNext()) {
                    StateEvent event = iterator.next();
                    // Remove expired events based on within
                    if (withinStates.size() > 0) {
                        if (isExpired(event, currentTime)) {
                            iterator.remove();
                            continue;
                        }
                    }
                    // Collect the events that came before the waiting time
                    if (currentTime >= event.getTimestamp() + waitingTime) {
                        iterator.remove();
                        event.setTimestamp(currentTime);
                        retEventChunk.add(event);
                    }
                }

                notProcessed = retEventChunk.getFirst() == null;
                while (retEventChunk.hasNext()) {
                    StateEvent stateEvent = retEventChunk.next();
                    retEventChunk.remove();
                    sendEvent(stateEvent);
                }
            }
            this.lastArrivalTime = 0;
        }
        if (thisStatePostProcessor.nextEveryStatePerProcessor == this || (notProcessed && isStartState)) {
            // If every or (notProcessed and startState), schedule again
            long nextBreak;
            if (lastArrivalTime == 0) {
                nextBreak = currentTime + waitingTime;
            } else {
                nextBreak = lastArrivalTime + waitingTime;
            }
            this.scheduler.notifyAt(nextBreak);
        }

    }

    private void sendEvent(StateEvent stateEvent) {
        if (thisStatePostProcessor.nextProcessor != null) {
            thisStatePostProcessor.nextProcessor.process(new ComplexEventChunk<>(stateEvent, stateEvent, false));
        }
        if (thisStatePostProcessor.nextStatePerProcessor != null) {
            thisStatePostProcessor.nextStatePerProcessor.addState(stateEvent);
        }
        if (thisStatePostProcessor.nextEveryStatePerProcessor != null) {
            thisStatePostProcessor.nextEveryStatePerProcessor.addEveryState(stateEvent);
        } else if (isStartState) {
            this.active = false;
        }
        if (thisStatePostProcessor.callbackPreStateProcessor != null) {
            thisStatePostProcessor.callbackPreStateProcessor.startStateReset();
        }
    }

    @Override
    public ComplexEventChunk<StateEvent> processAndReturn(ComplexEventChunk complexEventChunk) {

        if (!this.active) {
            return new ComplexEventChunk<>(false);
        }
        ComplexEventChunk<StateEvent> event = super.processAndReturn(complexEventChunk);

        StateEvent firstEvent = event.getFirst();
        if (firstEvent != null) {
            event = new ComplexEventChunk<>(false);
        }
        // Always return an empty event
        return event;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public PreStateProcessor cloneProcessor(String key) {
        AbsentStreamPreStateProcessor streamPreStateProcessor = new AbsentStreamPreStateProcessor(stateType,
                withinStates, waitingTimeConstant);
        cloneProperties(streamPreStateProcessor, key);
        streamPreStateProcessor.init(siddhiAppContext, queryName);

        // Set the scheduler
        siddhiAppContext.addEternalReferencedHolder(streamPreStateProcessor);
        EntryValveProcessor entryValveProcessor = new EntryValveProcessor(siddhiAppContext);
        entryValveProcessor.setToLast(streamPreStateProcessor);
        Scheduler scheduler = SchedulerParser.parse(siddhiAppContext.getScheduledExecutorService(),
                entryValveProcessor, siddhiAppContext);
        streamPreStateProcessor.setScheduler(scheduler);
        return streamPreStateProcessor;
    }

    @Override
    public void start() {
        // Start automatically only if it is the start state and 'for' time is defined
        // Otherwise, scheduler will be started in the addState method
        if (isStartState && waitingTime != -1 && active) {
            synchronized (this) {
                this.scheduler.notifyAt(this.siddhiAppContext.getTimestampGenerator().currentTime() + waitingTime);
            }
        }
    }

    @Override
    public void stop() {
        // Scheduler will be stopped automatically
        // Nothing to stop here
    }
}
