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
package org.wso2.siddhi.core.query.processor.join;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.BundleEvent;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListAtomicEvent;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StateEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.query.processor.PreSelectProcessingElement;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.util.LogHelper;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

//written in thinking of LEFT
// RIGHT is handled in the #createNewEvent()
public abstract class JoinProcessor implements QueryPostProcessingElement, PreSelectProcessingElement {
    static final Logger log = Logger.getLogger(JoinProcessor.class);
    private WindowProcessor windowProcessor;

    protected ConditionExecutor onConditionExecutor;
    private boolean triggerEvent;
    private WindowProcessor oppositeWindowProcessor;
    protected long within = -1;
    private boolean distributedProcessing;
    private QuerySelector querySelector;
    private Lock lock;
    private boolean fromDB = false;


    public WindowProcessor getWindowProcessor() {
        return windowProcessor;
    }

    public JoinProcessor(ConditionExecutor onConditionExecutor, boolean triggerEvent,
                         boolean distributedProcessing, Lock lock, boolean fromDB) {
        this.onConditionExecutor = onConditionExecutor;
        this.triggerEvent = triggerEvent;
        this.distributedProcessing = distributedProcessing;
        this.lock = lock;
        this.fromDB = fromDB;
    }


    private boolean isEventsWithin(ComplexEvent complexEvent, ComplexEvent windowComplexEvent) {
        if (within > -1) {
            long diff = Math.abs(complexEvent.getTimeStamp() - windowComplexEvent.getTimeStamp());
            if (diff > within || diff == Long.MIN_VALUE) {
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    protected void sendEventList(ListAtomicEvent list) {
        if (log.isDebugEnabled()) {
            log.debug(list);
        }
        int size = list.getActiveEvents();
        if (size > 1) {
            querySelector.process(list);
        } else if (size == 1) {
            querySelector.process(list.getEvent0());
        }
    }

    public void setOppositeWindowProcessor(WindowProcessor windowProcessor) {
        this.oppositeWindowProcessor = windowProcessor;
    }

    public void setWindowProcessor(WindowProcessor windowProcessor) {
        this.windowProcessor = windowProcessor;
    }

    public void setWithin(long within) {
        this.within = within;
    }


    public void setNext(QuerySelector querySelector) {
        this.querySelector = querySelector;
    }


    public void process(AtomicEvent atomicEvent) {

        LogHelper.logMethod(log, atomicEvent);
        if (atomicEvent instanceof Event && triggerEventTypeCheck((Event) atomicEvent)) {
            if (triggerEvent) {
                acquireLock();
                ListAtomicEvent listAtomicEvent = createNewListAtomicEvent();
                Iterator<StreamEvent> iterator = getStreamEventIterator((Event) atomicEvent);

                while (iterator.hasNext()) {
                    StreamEvent windowStreamEvent = iterator.next();

                    if (windowStreamEvent instanceof Event) {
//                        Event newEvent = (new InComplexEvent(new Event[]{((Event) complexEvent), ((Event) windowStreamEvent)}));
                        if (isEventsWithin((Event) atomicEvent, windowStreamEvent)) {
                            StateEvent newEvent = createNewEvent((Event) atomicEvent, (Event) windowStreamEvent);
                            if (onConditionExecutor.execute(newEvent)) {
                                listAtomicEvent.addEvent(newEvent);
                            }
                        } else {
                            break;
                        }
                    } else if (windowStreamEvent instanceof ListEvent) {
                        Event[] events = ((ListEvent) windowStreamEvent).getEvents();
                        for (Event event : events) {
//                            Event newEvent = (new InComplexEvent(new Event[]{((Event) complexEvent), ((Event) events[i])}));
                            if (isEventsWithin((Event) atomicEvent, windowStreamEvent)) {
                                StateEvent newEvent = createNewEvent((Event) atomicEvent, event);
                                if (onConditionExecutor.execute(newEvent)) {
                                    listAtomicEvent.addEvent(newEvent);
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        //todo error Complex atomicEvent not supported
                    }
                }
                if (listAtomicEvent.getActiveEvents() > 0) {
                    sendEventList(listAtomicEvent);
                }
                if (atomicEvent instanceof InStream) {
                    windowProcessor.process(atomicEvent);
                }
                releaseLock();
            } else {
                if (atomicEvent instanceof InStream) {
                    windowProcessor.process(atomicEvent);
                }
            }
        }
    }

    protected abstract boolean triggerEventTypeCheck(ComplexEvent complexEvent);

    public void process(BundleEvent bundleEvent) {
        //        System.out.println("Arrived");
        ListEvent listEvent = (ListEvent) bundleEvent;
        if (triggerEventTypeCheck(listEvent)) {
            if (triggerEvent) {
                ListAtomicEvent listAtomicEvent = createNewListAtomicEvent();
                if (log.isDebugEnabled()) {
                    log.debug("Joining input events " + listEvent.getActiveEvents());
                }
                acquireLock();
                try {
                    Iterator<StreamEvent> iterator = oppositeWindowProcessor.iterator();
                    while (iterator.hasNext()) {
                        StreamEvent windowStreamEvent = iterator.next();
                        //Assuming all events in complexEvent have time == to the timeStamp of the complexEvent.
                        if (isEventsWithin(listEvent, windowStreamEvent)) {
                            for (int i = 0; i < listEvent.getActiveEvents(); i++) {
                                Event event = listEvent.getEvent(i);
                                if (windowStreamEvent instanceof Event) {
                                    StateEvent newEvent = createNewEvent(event, windowStreamEvent);
                                    if (onConditionExecutor.execute(newEvent)) {
                                        listAtomicEvent.addEvent(newEvent);
                                    }
                                } else if (windowStreamEvent instanceof ListEvent) {
                                    for (int i1 = 0; i1 < ((ListEvent) windowStreamEvent).getActiveEvents(); i1++) {
                                        Event windowEvent = ((ListEvent) windowStreamEvent).getEvent(i1);
                                        StateEvent newEvent = createNewEvent(event, windowEvent);
                                        if (onConditionExecutor.execute(newEvent)) {
                                            listAtomicEvent.addEvent(newEvent);
                                        }
                                    }
                                } else {
                                    //todo error Complex event not supported
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Sending join output events " + listAtomicEvent.getActiveEvents());
                    }
                    sendEventList(listAtomicEvent);
                    if (listEvent instanceof InStream) {
                        windowProcessor.process(listEvent);
                    }
                } finally {
                    releaseLock();
                }
            } else {
                if (listEvent instanceof InStream) {
                    windowProcessor.process(listEvent);
                }
            }
        }

    }

    protected abstract ListAtomicEvent createNewListAtomicEvent();

    private Iterator<StreamEvent> getStreamEventIterator(Event event) {
        Iterator<StreamEvent> iterator;
        if (distributedProcessing) {
            StateEvent newEvent = createNewEvent(event, null);
            if (fromDB) {
                //Todo fix this based on the SQL impel
                iterator = oppositeWindowProcessor.iterator();
            } else {
                String sqlPredicate = onConditionExecutor.constructFilterQuery(newEvent, 1);
                if (within > -1) {
                    sqlPredicate = sqlPredicate + " and ( timeStamp < " + (event.getTimeStamp() + within) + ")";
                }
                log.debug("Join sql predicate: " + sqlPredicate);
                iterator = oppositeWindowProcessor.iterator(sqlPredicate);
            }
        } else {
            if (fromDB) {
                //todo fix
                if (event != null) {
                    iterator = oppositeWindowProcessor.iterator(event, onConditionExecutor);
                } else {
                    iterator = oppositeWindowProcessor.iterator();
                }
            } else {
                iterator = oppositeWindowProcessor.iterator();
            }
        }
        return iterator;
    }

    public void acquireLock() {
        if (lock != null) {
            if (log.isDebugEnabled()) {
                log.debug(lock + " trying to acquire join locked");
            }
            lock.lock();
            if (log.isDebugEnabled()) {
                log.debug(lock + " join locked");
            }
        }
    }

    public void releaseLock() {
        if (lock != null) {
            lock.unlock();
            if (log.isDebugEnabled()) {
                log.debug(lock + " join unlocked");
            }
        }
    }


    protected abstract StateEvent createNewEvent(ComplexEvent complexEvent, ComplexEvent complexEvent1);

}
