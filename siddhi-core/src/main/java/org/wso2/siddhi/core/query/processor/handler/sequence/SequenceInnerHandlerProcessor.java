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
package org.wso2.siddhi.core.query.processor.handler.sequence;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StateEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InStateEvent;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.snapshot.Snapshotable;
import org.wso2.siddhi.core.query.QueryElement;
import org.wso2.siddhi.core.query.processor.PreSelectProcessingElement;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.processor.handler.InnerHandlerProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.statemachine.sequence.OrSequenceState;
import org.wso2.siddhi.core.query.statemachine.sequence.SequenceState;
import org.wso2.siddhi.core.util.statemachine.statelist.StateList;
import org.wso2.siddhi.core.util.statemachine.statelist.StateListGrid;

import java.util.Collection;
import java.util.Map;

public class SequenceInnerHandlerProcessor
        implements InnerHandlerProcessor, PreSelectProcessingElement, QueryElement, Snapshotable {
    static final Logger log = Logger.getLogger(SequenceInnerHandlerProcessor.class);
    protected int complexEventSize;
    protected SequenceState state;
    protected SequenceState nextState;
    protected FilterProcessor filterProcessor;
    protected StateList<StateEvent> currentEvents;
    protected StateList<StateEvent> nextEvents;
    //    private final boolean first;
    protected final int currentState;
    protected String elementId;
    protected long within = -1;
    protected boolean distributedProcessing = false;
    protected SiddhiContext siddhiContext;
    protected int processedEventsToBeDropped[] = null;
    private QuerySelector querySelector;
    protected SequenceInnerHandlerProcessor nextPartnerStateSequenceInnerHandlerProcessor;
    protected SequenceInnerHandlerProcessor nextStateSequenceInnerHandlerProcessor;
    protected SequenceInnerHandlerProcessor partnerStateSequenceInnerHandlerProcessor;
    protected SequenceInnerHandlerProcessor stateSequenceInnerHandlerProcessor;

    public SequenceInnerHandlerProcessor(SequenceState state,
                                         FilterProcessor filterProcessor,
                                         int complexEventSize,
                                         SiddhiContext siddhiContext, String elementId) {
        this.state = state;
        this.nextState = state.getNextState();
        this.currentState = state.getStateNumber();
        this.complexEventSize = complexEventSize;
        this.filterProcessor = filterProcessor;
        this.distributedProcessing = siddhiContext.isDistributedProcessingEnabled();
        this.siddhiContext = siddhiContext;
        this.elementId = elementId;
        if (distributedProcessing) {
            currentEvents = new StateListGrid(elementId + "-currentState", siddhiContext);
            nextEvents = new StateListGrid(elementId + "-nextEvents", siddhiContext);
        } else {
            currentEvents = new StateList<StateEvent>();
            nextEvents = new StateList<StateEvent>();
        }
    }

    public void init(Map<Integer, SequenceInnerHandlerProcessor> stateSequenceInnerHandlerProcessorMap) {
        stateSequenceInnerHandlerProcessor = stateSequenceInnerHandlerProcessorMap.get(state.getStateNumber());
        if (state instanceof OrSequenceState) {
            partnerStateSequenceInnerHandlerProcessor = stateSequenceInnerHandlerProcessorMap.get(((OrSequenceState) state).getPartnerState().getStateNumber());
        }
        if (nextState != null) {
            nextStateSequenceInnerHandlerProcessor = stateSequenceInnerHandlerProcessorMap.get(nextState.getStateNumber());
            if (nextState instanceof OrSequenceState) {
                nextPartnerStateSequenceInnerHandlerProcessor = stateSequenceInnerHandlerProcessorMap.get(((OrSequenceState) nextState).getPartnerState().getStateNumber());
            }
        }

        if (state.isFirst()) {
            //first event
            if (distributedProcessing) {
                if (!nextEvents.isInited()) {
                    addToNextEvents(new InStateEvent(new StreamEvent[complexEventSize], siddhiContext.getGlobalIndexGenerator().getNewIndex()));
                }
            } else {
                addToNextEvents(new InStateEvent(new StreamEvent[complexEventSize]));
            }
        }
    }

    protected void reInit() {
        if (state.isFirst()) {
            //first event
            if (distributedProcessing) {
                addToNextEvents(new InStateEvent(new StreamEvent[complexEventSize], siddhiContext.getGlobalIndexGenerator().getNewIndex()));
            } else {
                addToNextEvents(new InStateEvent(new StreamEvent[complexEventSize]));
            }
        }
    }

    @Override
    public void process(StreamEvent streamEvent) {
        if (streamEvent instanceof ListEvent) {
            for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                sendForProcess(((ListEvent) streamEvent).getEvent(i));
                reInit();
            }
        } else {
            sendForProcess(streamEvent);
            reInit();
        }

    }

    protected void sendForProcess(StreamEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("sr state=" + currentState + " event=" + event + " ||eventBank=" + currentEvents);
        }
        for (StateEvent currentEvent : getCollection()) {
            if (isEventsWithin(event, currentEvent)) {
                currentEvent.setStreamEvent(currentState, event);
                StateEvent newEvent = (StateEvent) filterProcessor.process(currentEvent);
                if (newEvent != null) {
                    processSuccessEvent(newEvent);
                } else {

                    currentEvent.setStreamEvent(currentState, null);
                }
            }
        }
    }

    protected void processSuccessEvent(StateEvent stateEvent) {
        if (log.isDebugEnabled()) {
            log.debug("sp state=" + state.getStateNumber() + " event=" + stateEvent);
        }
        setEventState(stateEvent);
        if (state.isLast()) {
            sendEvent(stateEvent);
        }
        cleanUpEvent(stateEvent);
        passToNextStates(stateEvent);
    }

    protected void cleanUpEvent(StateEvent stateEvent) {
        if (processedEventsToBeDropped != null) {
            for (int i = 0; i < processedEventsToBeDropped.length; i++) {
                stateEvent.setStreamEvent(processedEventsToBeDropped[i], null);
            }
        }
    }

    protected Collection<StateEvent> getCollection() {
        Collection<StateEvent> collection;
        if (distributedProcessing) {
            if (within > -1) {
                collection = ((StateListGrid) currentEvents).getCollection("( timeStamp < " + (System.currentTimeMillis() + within) + ")");
            } else {
                collection = currentEvents.getCollection();
            }
        } else {
            collection = currentEvents.getCollection();
        }
        return collection;
    }

    protected boolean isEventsWithin(StreamEvent incomingEvent, StateEvent currentEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Time difference for Sequence events " + (incomingEvent.getTimeStamp() - currentEvent.getFirstEventTimeStamp()));
        }
        if (within == -1 || currentEvent.getFirstEventTimeStamp() == 0) {
            return true;
        } else if ((incomingEvent.getTimeStamp() - currentEvent.getFirstEventTimeStamp()) <= within) {
            return true;
        } else {
            return false;
        }
    }


    public String getStreamId() {
        return state.getTransformedStream().getStreamId();
    }

    public void addToNextEvents(StateEvent stateEvent) {
//        System.out.println("add to next ss");
        try {
            nextEvents.put(stateEvent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void moveNextEventsToCurrentEvents() {
        //todo need to check which is faster
        // 1
        currentEvents.clear();
        currentEvents.addAll(nextEvents.getAll());
        nextEvents.clear();

//        eventBank.clear();
//        eventBank.addAll(nextEvents);
//        nextEvents.clear();

//        // 2
//        eventBank = nextEvents;
//        nextEvents = new LinkedBlockingQueue<StateEvent>();
    }

    @Override
    public SnapshotObject snapshot() {
        return new SnapshotObject(currentEvents.currentState(), nextEvents.currentState());
    }

    @Override
    public void restore(SnapshotObject snapshotObject) {
        currentEvents.restoreState((Object[]) snapshotObject.getData()[0]);
        nextEvents.restoreState((Object[]) snapshotObject.getData()[1]);

    }

    public void setWithin(long within) {
        this.within = within;
    }

    public void updateToCurrentEvents(StateEvent updateContainingStateEvent, int updatingState) {
        ((StateListGrid) currentEvents).update(updateContainingStateEvent, updatingState);
    }

    public void updateToNextEvents(StateEvent updateContainingStateEvent, int updatingState) {
        ((StateListGrid) nextEvents).update(updateContainingStateEvent, updatingState);
    }

    public void removeFromCurrentEvents(StateEvent removingStateEvent) {
        ((StateListGrid) currentEvents).remove(removingStateEvent);
    }

    public void removeFromNextEvents(StateEvent removingStateEvent) {
        ((StateListGrid) nextEvents).remove(removingStateEvent);
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }


    protected void setEventState(StateEvent eventBundle) {
        eventBundle.setEventState(state.getStateNumber());
    }

    protected void sendEvent(AtomicEvent atomicEvent) {
        querySelector.process(atomicEvent);
    }

    protected void passToNextStates(StateEvent eventBundle) {
        if (nextState != null) {
            if (log.isDebugEnabled()) {
                log.debug("->" + nextState.getStateNumber());
            }
            if (nextState instanceof OrSequenceState) {
                if (log.isDebugEnabled()) {
                    log.debug("->" + ((OrSequenceState) nextState).getPartnerState().getStateNumber());
                }
                nextPartnerStateSequenceInnerHandlerProcessor.addToNextEvents(eventBundle);
            }
            nextStateSequenceInnerHandlerProcessor.addToNextEvents(eventBundle);
        }
    }

    public void setNext(QuerySelector querySelector) {
        this.querySelector = querySelector;
    }

    public void reset() {
        currentEvents.clear();
        nextEvents.clear();
        reInit();
    }

    @Override
    public int getCurrentStateNumber() {
        return currentState;
    }

    @Override
    public void setProcessedEventsToBeDropped(int[] processedEventsToBeDropped) {
        this.processedEventsToBeDropped = processedEventsToBeDropped;
    }

}
