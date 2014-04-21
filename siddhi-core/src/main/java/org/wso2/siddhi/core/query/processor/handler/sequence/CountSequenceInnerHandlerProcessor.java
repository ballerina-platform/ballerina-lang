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
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StateEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.statemachine.sequence.CountSequenceState;
import org.wso2.siddhi.core.query.statemachine.sequence.OrSequenceState;
import org.wso2.siddhi.core.util.statemachine.statelist.StateListGrid;
import org.wso2.siddhi.query.api.utils.SiddhiConstants;

public class CountSequenceInnerHandlerProcessor extends SequenceInnerHandlerProcessor {
    static final Logger log = Logger.getLogger(CountSequenceInnerHandlerProcessor.class);

    private int min = -1;
    private int max = -1;

    public CountSequenceInnerHandlerProcessor(CountSequenceState state,
                                              FilterProcessor firstSimpleQueryStreamProcessor,
                                              int complexEventSize,
                                              SiddhiContext siddhiContext, String elementId) {
        super(state, firstSimpleQueryStreamProcessor, complexEventSize, siddhiContext, elementId);
        this.min = state.getMin();
        this.max = state.getMax();


    }

    protected void sendForProcess(StreamEvent streamEvent) {
        if (log.isDebugEnabled()) {
            log.debug("cr state=" + currentState + " event=" + streamEvent + " ||eventBank=" + currentEvents);
        }
        for (StateEvent currentEvent : getCollection()) {
            if (isEventsWithin(streamEvent, currentEvent)) {
                if (currentEvent.getEventState() <= (state.getStateNumber())) {
                    ListEvent listEvent = (ListEvent) currentEvent.getStreamEvent(currentState);
                    if (listEvent == null) {
                        if (max == SiddhiConstants.UNLIMITED) {
                            listEvent = new InListEvent();
                        } else {
                            listEvent = new InListEvent(max);
                        }
                        currentEvent.setStreamEvent(currentState, listEvent);
                    }
                    if (!listEvent.addEvent(((Event) streamEvent))) {
                        continue;
                    }
                    StateEvent newEvent = (StateEvent) filterProcessor.process(currentEvent);
                    if (newEvent != null) {
                        processSuccessEvent(newEvent);
                    } else {
                        listEvent.removeLast();  //to stop aggregation of not passed events
                    }
                }
            }
        }
    }


    public synchronized void addToNextEvents(StateEvent stateEvent) {
        if (min == 0) {
            stateSequenceInnerHandlerProcessor.processSuccessEvent(stateEvent);
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
            if (nextState instanceof OrSequenceState) {
                if (log.isDebugEnabled()) {
                    log.debug("update ->" + ((OrSequenceState) nextState).getPartnerState().getStateNumber());
                }
                nextPartnerStateSequenceInnerHandlerProcessor.updateToNextEvents(updateContainingStateEvent, updatingState);
            }
            nextStateSequenceInnerHandlerProcessor.updateToNextEvents(updateContainingStateEvent, updatingState);
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

    public void processSuccessEvent(StateEvent stateEvent) {
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
            if (state.isLast()) {
                sendEvent(stateEvent);
            }
            //passToStreamReceivers
            if (activeEvents < ((CountSequenceState) state).getMin()) {        // if the active event count is not enough; add to next event of CURRENT state
                if (log.isDebugEnabled()) {
                    log.debug("->" + state.getStateNumber());
                }
                addOnlyToNextEvents((StateEvent) stateEvent);
            } else if (activeEvents >= ((CountSequenceState) state).getMin() && activeEvents <= ((CountSequenceState) state).getMax() || activeEvents >= ((CountSequenceState) state).getMin() && ((CountSequenceState) state).getMax() == SiddhiConstants.UNLIMITED) {
                // active event count is enough; add to next event of CURRENT and NEXT states
                if (nextState != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("->" + nextState.getStateNumber());
                    }
                    StateEvent clonedEvent = null;
                    if (distributedProcessing) {
                        clonedEvent = stateEvent.cloneEvent(nextState.getStateNumber(), siddhiContext.getGlobalIndexGenerator().getNewIndex());
                    } else {
                        clonedEvent = stateEvent.cloneEvent(nextState.getStateNumber(), null);
                    }
                    cleanUpEvent(clonedEvent);
                    if (nextState instanceof OrSequenceState) {
                        if (log.isDebugEnabled()) {
                            log.debug("->" + ((OrSequenceState) nextState).getPartnerState().getStateNumber());
                        }
                        nextPartnerStateSequenceInnerHandlerProcessor.addToNextEvents(clonedEvent);
                    }
                    nextStateSequenceInnerHandlerProcessor.addToNextEvents(clonedEvent);
                    addOnlyToNextEvents(stateEvent);
                }
//            } else if (activeEvents >= ((CountSequenceState) state).getMin() && activeEvents <= ((CountSequenceState) state).getMax()||activeEvents >= ((CountSequenceState) state).getMin() && ((CountSequenceState) state).getMax()==RegexElement.UNLIMITED) {
//                System.out.println("->" + state.getStateNumber());
//                ((CountSequenceInnerHandlerProcessor) streamAnalyser).addOnlyToNextEvents((StateEvent) stateEvent);
////                if (state.getNextStateListSize() > 0) {
////                    for (State nextState : state.getNextStates()) {
////                        StateEvent stateEvent = ((StateEvent) stateEvent).cloneEvent();
////                        if (nextState instanceof LogicState) {
////                            ((LogicState) nextState).getPartnerState().getInnerHandlerProcessor().addToEventBank(stateEvent);
////                        }
////                        nextState.getInnerHandlerProcessor().addToEventBank(stateEvent);
////                    }
////                }

            }
        }
    }
}
