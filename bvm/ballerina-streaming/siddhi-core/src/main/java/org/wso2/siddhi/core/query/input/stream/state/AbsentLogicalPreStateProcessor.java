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
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.parser.SchedulerParser;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logical not processor.
 */
public class AbsentLogicalPreStateProcessor extends LogicalPreStateProcessor implements AbsentPreStateProcessor {

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
    private volatile long lastArrivalTime;

    /**
     * Lock to synchronize the both {@link AbsentLogicalPreStateProcessor}s in the pattern.
     */
    private ReentrantLock lock;

    /**
     * This flag turns to false after processing the first event if 'every' is not used.
     */
    private boolean active = true;

    /**
     * TimeConstant to be used by cloneProcessor method.
     */
    private TimeConstant waitingTimeConstant;

    public AbsentLogicalPreStateProcessor(LogicalStateElement.Type type, StateInputStream.Type stateType,
                                          List<Map.Entry<Long, Set<Integer>>> withinStates, TimeConstant waitingTime) {
        super(type, stateType, withinStates);
        if (waitingTime != null) {
            this.waitingTime = waitingTime.value();
            this.waitingTimeConstant = waitingTime;
        }
    }

    @Override
    public void setPartnerStatePreProcessor(LogicalPreStateProcessor partnerStatePreProcessor) {
        super.setPartnerStatePreProcessor(partnerStatePreProcessor);
        if (this.lock == null) {
            this.lock = new ReentrantLock();
        }
        if (partnerStatePreProcessor instanceof AbsentLogicalPreStateProcessor && ((AbsentLogicalPreStateProcessor)
                partnerStatePreProcessor).lock == null) {
            ((AbsentLogicalPreStateProcessor) partnerStatePreProcessor).lock = this.lock;
        }
    }

    @Override
    public void updateLastArrivalTime(long timestamp) {

        this.lock.lock();
        try {
            this.lastArrivalTime = timestamp;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void addState(StateEvent stateEvent) {

        if (!this.active) {
            return;
        }
        this.lock.lock();
        try {
            super.addState(stateEvent);
            if (!isStartState) {
                if (waitingTime != -1) {
                    scheduler.notifyAt(stateEvent.getTimestamp() + waitingTime);
                    if (partnerStatePreProcessor instanceof AbsentLogicalPreStateProcessor) {
                        ((AbsentLogicalPreStateProcessor) partnerStatePreProcessor).scheduler.notifyAt(stateEvent
                                .getTimestamp() + ((AbsentLogicalPreStateProcessor) partnerStatePreProcessor)
                                .waitingTime);
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void addEveryState(StateEvent stateEvent) {

        StateEvent clonedEvent = stateEventCloner.copyStateEvent(stateEvent);
        if (clonedEvent.getStreamEvent(stateId) != null) {
            // Set the timestamp of the last arrived event
            clonedEvent.setTimestamp(clonedEvent.getStreamEvent(stateId).getTimestamp());
        }
        clonedEvent.setEvent(stateId, null);
        clonedEvent.setEvent(partnerStatePreProcessor.stateId, null);
        // Start state takes events from newAndEveryStateEventList
        newAndEveryStateEventList.add(clonedEvent);
        partnerStatePreProcessor.newAndEveryStateEventList.add(clonedEvent);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

        if (!this.active) {
            return;
        }
        this.lock.lock();
        boolean notProcessed = true;
        try {
            long currentTime = complexEventChunk.getFirst().getTimestamp();
            if (currentTime >= this.lastArrivalTime + waitingTime) {

                ComplexEventChunk<StateEvent> retEventChunk = new ComplexEventChunk<>(false);

                Iterator<StateEvent> iterator;
                if (isStartState && stateType == StateInputStream.Type.SEQUENCE && newAndEveryStateEventList.isEmpty()
                        && pendingStateEventList.isEmpty()) {

                    StateEvent stateEvent = stateEventPool.borrowEvent();
                    addState(stateEvent);
                } else if (stateType == StateInputStream.Type.SEQUENCE && !newAndEveryStateEventList.isEmpty()) {
                    this.resetState();
                }

                this.updateState();
                iterator = pendingStateEventList.iterator();

                while (iterator.hasNext()) {
                    StateEvent stateEvent = iterator.next();

                    // Remove expired events based on within
                    if (withinStates.size() > 0) {
                        if (isExpired(stateEvent, currentTime)) {
                            iterator.remove();
                            continue;
                        }
                    }

                    // Collect the events that came before the waiting time
                    if (waitingTimePassed(currentTime, stateEvent)) {

                        iterator.remove();

                        if (logicalType == LogicalStateElement.Type.OR && stateEvent.getStreamEvent
                                (partnerStatePreProcessor.getStateId()) == null) {
                            // OR Partner not received
                            stateEvent.addEvent(stateId, streamEventPool.borrowEvent());
                            retEventChunk.add(stateEvent);
                        } else if (logicalType == LogicalStateElement.Type.AND && stateEvent.getStreamEvent
                                (partnerStatePreProcessor.getStateId()) != null) {
                            // AND partner received but didn't send out
                            retEventChunk.add(stateEvent);
                        } else if (logicalType == LogicalStateElement.Type.AND && stateEvent.getStreamEvent
                                (partnerStatePreProcessor.getStateId()) == null) {
                            // AND partner didn't receive
                            // Let the partner to process or not
                            stateEvent.addEvent(stateId, streamEventPool.borrowEvent());
                        }
                    }
                }

                retEventChunk.reset();
                notProcessed = retEventChunk.getFirst() == null;
                while (retEventChunk.hasNext()) {
                    StateEvent stateEvent = retEventChunk.next();
                    retEventChunk.remove();
                    sendEvent(stateEvent);
                }

                this.lastArrivalTime = 0;
            }
        } finally {
            this.lock.unlock();
        }

        if (thisStatePostProcessor.nextEveryStatePerProcessor != null || (notProcessed && isStartState)) {
            // If every or (notProcessed and startState), schedule again
            long nextBreak;
            if (lastArrivalTime == 0) {
                nextBreak = siddhiAppContext.getTimestampGenerator().currentTime() + waitingTime;
            } else {
                nextBreak = lastArrivalTime + waitingTime;
            }
            this.scheduler.notifyAt(nextBreak);
        }
    }

    private boolean waitingTimePassed(long currentTime, StateEvent event) {
        if (event.getStreamEvent(stateId) == null) {
            // Not processed already
            return currentTime >= event.getTimestamp() + waitingTime;
        } else {
            // Already processed by this processor and added back due to 'every'
            return currentTime >= event.getStreamEvent(stateId).getTimestamp() + waitingTime;
        }
    }

    private void sendEvent(StateEvent stateEvent) {
        if (thisStatePostProcessor.nextProcessor != null) {
            thisStatePostProcessor.nextProcessor.process(new ComplexEventChunk<>(stateEvent,
                    stateEvent, false));
        }
        if (thisStatePostProcessor.nextStatePerProcessor != null) {
            thisStatePostProcessor.nextStatePerProcessor.addState(stateEvent);
        }
        if (thisStatePostProcessor.nextEveryStatePerProcessor != null) {
            thisStatePostProcessor.nextEveryStatePerProcessor.addEveryState(stateEvent);
        } else if (isStartState) {
            this.active = false;
            if (logicalType == LogicalStateElement.Type.OR &&
                    partnerStatePreProcessor instanceof AbsentLogicalPreStateProcessor) {
                ((AbsentLogicalPreStateProcessor) partnerStatePreProcessor).active = false;
            }
        }
        if (thisStatePostProcessor.callbackPreStateProcessor != null) {
            thisStatePostProcessor.callbackPreStateProcessor.startStateReset();
        }
    }

    @Override
    public ComplexEventChunk<StateEvent> processAndReturn(ComplexEventChunk complexEventChunk) {

        ComplexEventChunk<StateEvent> returnEventChunk = new ComplexEventChunk<>(false);

        if (!this.active) {
            return returnEventChunk;
        }
        complexEventChunk.reset();
        StreamEvent streamEvent = (StreamEvent) complexEventChunk.next(); //Sure only one will be sent

        this.lock.lock();
        try {
            for (Iterator<StateEvent> iterator = pendingStateEventList.iterator(); iterator.hasNext(); ) {
                StateEvent stateEvent = iterator.next();
                if (withinStates.size() > 0) {
                    if (isExpired(stateEvent, streamEvent.getTimestamp())) {
                        iterator.remove();
                        continue;
                    }
                }
                if (logicalType == LogicalStateElement.Type.OR &&
                        stateEvent.getStreamEvent(partnerStatePreProcessor.getStateId()) != null) {
                    iterator.remove();
                    continue;
                }
                StreamEvent currentStreamEvent = stateEvent.getStreamEvent(stateId);
                stateEvent.setEvent(stateId, streamEventCloner.copyStreamEvent(streamEvent));
                process(stateEvent);
                if (waitingTime != -1 || (stateType == StateInputStream.Type.SEQUENCE &&
                        logicalType == LogicalStateElement.Type.AND && thisStatePostProcessor
                        .nextEveryStatePerProcessor != null)) {
                    // Reset to the original state after processing
                    stateEvent.setEvent(stateId, currentStreamEvent);
                }
                if (this.thisLastProcessor.isEventReturned()) {
                    this.thisLastProcessor.clearProcessedEvent();
                    // The event has passed the filter condition. So remove from being an absent candidate.
                    iterator.remove();
                    if (stateType == StateInputStream.Type.SEQUENCE) {
                        partnerStatePreProcessor.pendingStateEventList.remove(stateEvent);
                    }
                }
                if (!stateChanged) {
                    switch (stateType) {
                        case PATTERN:
                            stateEvent.setEvent(stateId, currentStreamEvent);
                            break;
                        case SEQUENCE:
                            stateEvent.setEvent(stateId, currentStreamEvent);
                            iterator.remove();
                            break;
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
        return returnEventChunk;
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
        AbsentLogicalPreStateProcessor logicalPreStateProcessor = new AbsentLogicalPreStateProcessor(logicalType,
                stateType, withinStates, waitingTimeConstant);
        cloneProperties(logicalPreStateProcessor, key);
        logicalPreStateProcessor.init(siddhiAppContext, queryName);

        // Set the scheduler
        siddhiAppContext.addEternalReferencedHolder(logicalPreStateProcessor);
        EntryValveProcessor entryValveProcessor = new EntryValveProcessor(siddhiAppContext);
        entryValveProcessor.setToLast(logicalPreStateProcessor);
        Scheduler scheduler = SchedulerParser.parse(siddhiAppContext.getScheduledExecutorService(),
                entryValveProcessor, siddhiAppContext);
        logicalPreStateProcessor.setScheduler(scheduler);

        return logicalPreStateProcessor;
    }

    @Override
    public void start() {
        if (isStartState && waitingTime != -1 && active) {
            this.lock.lock();
            try {
                this.scheduler.notifyAt(this.siddhiAppContext.getTimestampGenerator().currentTime() +
                        waitingTime);
            } finally {
                this.lock.unlock();
            }
        }
    }

    @Override
    public void stop() {
        // Scheduler will be stopped automatically
        // Nothing to stop here
    }

    public boolean partnerCanProceed(StateEvent stateEvent) {

        boolean process;
        if (stateType == StateInputStream.Type.SEQUENCE && thisStatePostProcessor.nextEveryStatePerProcessor == null
                && this.lastArrivalTime > 0) {
            process = false;
        } else {
            if (this.waitingTime == -1) {
                // for time is not defined and event is not received by absent processor
                if (thisStatePostProcessor.nextEveryStatePerProcessor == null) {
                    process = stateEvent.getStreamEvent(this.stateId) == null;
                } else {
                    // Every
                    if (this.lastArrivalTime > 0) {
                        process = false;
                        this.lastArrivalTime = 0;
                        this.init();
                    } else {
                        process = true;
                    }
                }
            } else if (stateEvent.getStreamEvent(this.stateId) != null) {
                // for time is defined
                process = true;
            } else {
                process = false;
            }
        }

        return process;
    }
}
