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
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StateEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.statemachine.pattern.CountPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.LogicPatternState;
import org.wso2.siddhi.core.util.statemachine.statelist.StateListGrid;
import org.wso2.siddhi.query.api.utils.SiddhiConstants;

public class CountPatternInnerHandlerProcessor extends PatternInnerHandlerProcessor {
    static final Logger log = Logger.getLogger(CountPatternInnerHandlerProcessor.class);
    private int min = -1;
    private int max = -1;

    public CountPatternInnerHandlerProcessor(CountPatternState state,
                                             FilterProcessor firstSimpleQueryStreamProcessor,
                                             int complexEventSize, SiddhiContext siddhiContext, String elementId) {
        super(state, firstSimpleQueryStreamProcessor, complexEventSize, siddhiContext, elementId);
        this.min = state.getMin();
        this.max = state.getMax();

    }

    @Override
    public void process(StreamEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("cr state=" + currentState + " event=" + event + " ||eventBank=" + currentEvents);
        }
        for (StateEvent currentEvent : getCollection()) {
            if (isEventsWithin(event, currentEvent)) {
                if (currentEvent.getEventState() <= (state.getStateNumber())) {
                    ListEvent listEvent = (ListEvent) currentEvent.getStreamEvent(currentState);
                    if (listEvent == null) {
                        listEvent = new InListEvent(max == SiddhiConstants.UNLIMITED ? min : max);
                        currentEvent.setStreamEvent(currentState, listEvent);
                    }
//                System.out.println("---" + currentEvent);
                    if (!listEvent.addEvent(((Event) event))) {
                        continue;
                    }
                    StateEvent newEvent = (StateEvent) filterProcessor.process(currentEvent);
                    if (newEvent != null) {
                        processSuccessEvent(newEvent);
                    } else {
                        listEvent.removeLast();
                        addToNextEvents(currentEvent);
                    }
                }
            }
        }
    }

    public synchronized void addToNextEvents(StateEvent stateEvent) {
        if (min == 0) {
            stateInnerHandlerProcessor.processSuccessEvent(stateEvent);
        } else {
            try {
                nextEvents.put(stateEvent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateToNextEvents(StateEvent updateContainingStateEvent, int updatingState) {
//        System.out.println("add to next ss");
        ((StateListGrid) nextEvents).update(updateContainingStateEvent, updatingState);
        if (nextState != null) {
            if (log.isDebugEnabled()) {
                log.debug("update ->" + nextState.getStateNumber());
            }
            if (nextState instanceof LogicPatternState) {
                if (log.isDebugEnabled()) {
                    log.debug("update ->" + ((LogicPatternState) nextState).getPartnerState().getStateNumber());
                }
                nextPartnerStateInnerHandlerProcessor.updateToNextEvents(updateContainingStateEvent, updatingState);
            }
            nextStateInnerHandlerProcessor.updateToNextEvents(updateContainingStateEvent, updatingState);
        }
    }

    public void addOnlyToNextEvents(StateEvent stateEvent) {
        try {
            nextEvents.put(stateEvent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    protected void processSuccessEvent(StateEvent stateEvent) {
        if (log.isDebugEnabled()) {
            log.debug("cp state=" + state.getStateNumber() + " event=" + stateEvent);
        }
        if (stateEvent.getEventState() <= (state.getStateNumber())) {
            setEventState(stateEvent);
            int activeEvents = 0;
            ListEvent listEvent = ((ListEvent) stateEvent.getStreamEvent(state.getStateNumber()));
            if (listEvent != null) {
                activeEvents = listEvent.getActiveEvents();
            }
//            System.out.println(" active "+activeEvents);
//            int activeEvents = ((SingleEventList) ((StateEvent) stateEvent).getStreamEvent(state.getStateNumber())).getActiveEvents();
            if (state.isLast()) {
                sendEvent(stateEvent);
            }


            //passToStreamReceivers
            if (activeEvents < ((CountPatternState) state).getMin()) {
                if (log.isDebugEnabled()) {
                    log.debug("->" + state.getStateNumber());
                }
                addOnlyToNextEvents(stateEvent);
            } else if (activeEvents == ((CountPatternState) state).getMin()) {
                if (nextState != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("->" + nextState.getStateNumber());
                    }
                    if (nextState instanceof LogicPatternState) {
                        if (log.isDebugEnabled()) {
                            log.debug("->" + ((LogicPatternState) nextState).getPartnerState().getStateNumber());
                        }
                        nextPartnerStateInnerHandlerProcessor.addToNextEvents((StateEvent) stateEvent);
                    }
                    nextStateInnerHandlerProcessor.addToNextEvents(stateEvent);
                    addOnlyToNextEvents(stateEvent);
                }
                if (nextEveryState != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("->" + nextEveryState.getStateNumber());
                    }
                    StateEvent newStateEvent;
                    if (distributedProcessing) {
                        newStateEvent = stateEvent.cloneEvent(nextEveryState.getStateNumber(), siddhiContext.getGlobalIndexGenerator().getNewIndex());
                    } else {
                        newStateEvent = stateEvent.cloneEvent(nextEveryState.getStateNumber(), null);
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
            } else if (activeEvents >= ((CountPatternState) state).getMin() && activeEvents <= ((CountPatternState) state).getMax() || activeEvents >= ((CountPatternState) state).getMin() && ((CountPatternState) state).getMax() == SiddhiConstants.UNLIMITED) {
                cleanUpEvent(stateEvent);
                addOnlyToNextEvents(stateEvent);
                if (distributedProcessing) {
                    if (nextState != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("update ->" + nextState.getStateNumber());
                        }
                        if (nextState instanceof LogicPatternState) {
                            if (log.isDebugEnabled()) {
                                log.debug("update ->" + ((LogicPatternState) nextState).getPartnerState().getStateNumber());
                            }
                            nextPartnerStateInnerHandlerProcessor.updateToNextEvents((StateEvent) stateEvent, state.getStateNumber());
                        }
                        nextStateInnerHandlerProcessor.updateToNextEvents(stateEvent, state.getStateNumber());
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("->" + state.getStateNumber());
                }
            }
        }
    }
}
