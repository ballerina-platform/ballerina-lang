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
package org.wso2.siddhi.core.query.processor.handler.pattern;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.StateEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InStateEvent;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.persistence.PersistenceStore;
import org.wso2.siddhi.core.snapshot.Snapshotable;
import org.wso2.siddhi.core.query.processor.PreSelectProcessingElement;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.processor.handler.InnerHandlerProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.statemachine.pattern.LogicPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.PatternState;
import org.wso2.siddhi.core.util.LogHelper;
import org.wso2.siddhi.core.util.statemachine.statelist.StateList;
import org.wso2.siddhi.core.util.statemachine.statelist.StateListGrid;

import java.util.Collection;
import java.util.Map;

public class PatternInnerHandlerProcessor
        implements InnerHandlerProcessor, PreSelectProcessingElement, Snapshotable {

    static final Logger log = Logger.getLogger(PatternInnerHandlerProcessor.class);
    protected final int currentState;
    protected int complexEventSize;
    protected PatternState state;
    protected PatternState nextState;
    protected PatternState nextEveryState;
    protected FilterProcessor filterProcessor;
    protected StateList<StateEvent> currentEvents;
    protected StateList<StateEvent> nextEvents;
    protected String elementId;
    protected boolean distributedProcessing;
    protected int processedEventsToBeDropped[] = null;
    protected SiddhiContext siddhiContext;
    protected QuerySelector querySelector;
    protected PatternInnerHandlerProcessor nextEveryStateInnerHandlerProcessor;
    protected PatternInnerHandlerProcessor nextEveryPartnerStateInnerHandlerProcessor;
    protected PatternInnerHandlerProcessor nextPartnerStateInnerHandlerProcessor;
    protected PatternInnerHandlerProcessor nextStateInnerHandlerProcessor;
    protected PatternInnerHandlerProcessor partnerStateInnerHandlerProcessor;
    protected PatternInnerHandlerProcessor stateInnerHandlerProcessor;
    private long within = -1;


    public PatternInnerHandlerProcessor(PatternState state,
                                        FilterProcessor filterProcessor,
                                        int complexEventSize, SiddhiContext siddhiContext, String elementId) {
        this.state = state;
        this.elementId = elementId;
        this.nextState = state.getNextState();
        this.nextEveryState = state.getNextEveryState();
        this.currentState = state.getStateNumber();
        this.complexEventSize = complexEventSize;
        this.distributedProcessing = siddhiContext.isDistributedProcessingEnabled();
        this.siddhiContext = siddhiContext;
        this.filterProcessor = filterProcessor;
        this.elementId = siddhiContext.getElementIdGenerator().createNewId();
        if (distributedProcessing) {
            currentEvents = new StateListGrid(this.elementId + "-eventBank", siddhiContext);
            nextEvents = new StateListGrid(this.elementId + "-nextEvents", siddhiContext);
        } else {
            currentEvents = new StateList<StateEvent>();
            nextEvents = new StateList<StateEvent>();
        }
    }

    public void init(Map<Integer, PatternInnerHandlerProcessor> statePatternInnerHandlerProcessorMap) {
        stateInnerHandlerProcessor = statePatternInnerHandlerProcessorMap.get(state.getStateNumber());
        if (state instanceof LogicPatternState) {
            partnerStateInnerHandlerProcessor = statePatternInnerHandlerProcessorMap.get(((LogicPatternState) state).getPartnerState().getStateNumber());
        }
        if (nextState != null) {
            nextStateInnerHandlerProcessor = statePatternInnerHandlerProcessorMap.get(nextState.getStateNumber());
            if (nextState instanceof LogicPatternState) {
                nextPartnerStateInnerHandlerProcessor = statePatternInnerHandlerProcessorMap.get(((LogicPatternState) nextState).getPartnerState().getStateNumber());
            }
        }
        if (nextEveryState != null) {
            nextEveryStateInnerHandlerProcessor = statePatternInnerHandlerProcessorMap.get(nextEveryState.getStateNumber());
            if (nextEveryState instanceof LogicPatternState) {
                nextEveryPartnerStateInnerHandlerProcessor = statePatternInnerHandlerProcessorMap.get(((LogicPatternState) nextEveryState).getPartnerState().getStateNumber());
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

    @Override
    public void process(StreamEvent event) {
        try {
            LogHelper.logMethod(log, event);

            Collection<StateEvent> collection = getCollection();
            if (log.isDebugEnabled()) {
                LogHelper.debugLogMessage(log, event, "current events:" + collection.size());
            }
            for (StateEvent currentEvent : collection) {
                if (log.isDebugEnabled()) {
                    LogHelper.debugLogMessage(log, event, "processing with event:" + currentEvent);
                }
                if (isEventsWithin(event, currentEvent)) {
                    currentEvent.setStreamEvent(currentState, event);
                    StateEvent newEvent = (StateEvent) filterProcessor.process(currentEvent);
                    if (newEvent != null) {
                        processSuccessEvent(newEvent);
                    } else {
                        currentEvent.setStreamEvent(currentState, null);
                        addToNextEvents(currentEvent);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void processSuccessEvent(StateEvent stateEvent) {
        LogHelper.logMethod(log, (AtomicEvent) stateEvent);
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
            LogHelper.debugLogMessage(log, incomingEvent, "time difference for Pattern events " + (incomingEvent.getTimeStamp() - currentEvent.getFirstEventTimeStamp()));
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

    @Override
    public void addToNextEvents(StateEvent stateEvent) {
        LogHelper.logMethod(log, (AtomicEvent) stateEvent);
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

//        // 2
//        eventBank = nextEvents;
//        nextEvents = new LinkedBlockingQueue<StateEvent>();
    }

    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
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

    protected void setEventState(StateEvent eventBundle) {
        if (log.isDebugEnabled()) {
            LogHelper.debugLogMessage(log, (ComplexEvent) eventBundle, "moves to state -> " + state.getStateNumber());
        }
        eventBundle.setEventState(state.getStateNumber());
    }

    protected void sendEvent(AtomicEvent atomicEvent) {
        querySelector.process(atomicEvent);
    }

    public void setNext(QuerySelector querySelector) {
        this.querySelector = querySelector;
    }

    protected void passToNextStates(StateEvent eventBundle) {
        if (nextState != null) {
            if (log.isDebugEnabled()) {
                log.debug("->" + nextState.getStateNumber());
            }
            if (nextState instanceof LogicPatternState) {
                if (log.isDebugEnabled()) {
                    log.debug("->" + ((LogicPatternState) nextState).getPartnerState().getStateNumber());
                }
                nextPartnerStateInnerHandlerProcessor.addToNextEvents(eventBundle);
            }
            nextStateInnerHandlerProcessor.addToNextEvents(eventBundle);
        }
        if (nextEveryState != null) {
            if (log.isDebugEnabled()) {
                log.debug("->" + nextEveryState.getStateNumber());
            }
            StateEvent newStateEvent;
            if (distributedProcessing) {
                newStateEvent = eventBundle.cloneEvent(nextEveryState.getStateNumber(), siddhiContext.getGlobalIndexGenerator().getNewIndex());
            } else {
                newStateEvent = eventBundle.cloneEvent(nextEveryState.getStateNumber(), null);
            }
            newStateEvent.setEventState(nextEveryState.getStateNumber() - 1);
            if (nextEveryState instanceof LogicPatternState) {
                if (log.isDebugEnabled()) {
                    log.debug("->" + ((LogicPatternState) nextEveryState).getPartnerState().getStateNumber());
                }
                nextEveryPartnerStateInnerHandlerProcessor.addToNextEvents(newStateEvent);
            }
            nextEveryStateInnerHandlerProcessor.addToNextEvents(newStateEvent);
        }

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
