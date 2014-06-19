/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.table;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.*;
import org.wso2.siddhi.core.event.in.InStateEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.persistence.PersistenceStore;
import org.wso2.siddhi.core.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;
import org.wso2.siddhi.core.util.collection.list.SiddhiListGrid;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.ArrayList;
import java.util.Iterator;

public class InMemoryEventTable implements EventTable, Snapshotable {
    static final Logger log = Logger.getLogger(InMemoryEventTable.class);

    private TableDefinition tableDefinition;
    private SiddhiList<StreamEvent> list;
    private QueryEventSource queryEventSource;
    private String elementId;
    private final boolean enableRemoveAndAdd;


    public InMemoryEventTable(TableDefinition tableDefinition, SiddhiContext siddhiContext) {
        elementId = siddhiContext.getElementIdGenerator().createNewId();
        this.tableDefinition = tableDefinition;
        this.queryEventSource = new QueryEventSource(tableDefinition.getTableId(), tableDefinition.getTableId(), tableDefinition, null, null, null);
        if (siddhiContext.isDistributedProcessingEnabled()) {
            enableRemoveAndAdd = true;
            this.list = new SiddhiListGrid<StreamEvent>(elementId, siddhiContext);
        } else {
            enableRemoveAndAdd = false;
            this.list = new SiddhiList<StreamEvent>();
        }

    }

    public synchronized void add(StreamEvent streamEvent) {
        if (streamEvent instanceof AtomicEvent) {
            list.add(new RemoveEvent((Event) streamEvent, Long.MAX_VALUE));
        } else {
            for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                list.add(new RemoveEvent((Event) ((ListEvent) streamEvent).getEvent(i), Long.MAX_VALUE));
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public synchronized void delete(StreamEvent streamEvent, ConditionExecutor conditionExecutor) {
        if (conditionExecutor != null) {
            if (streamEvent instanceof AtomicEvent) {
                Iterator<StreamEvent> iterator = list.iterator();
                StateEvent stateEvent = new InStateEvent(new StreamEvent[]{streamEvent, null});
                while (iterator.hasNext()) {
                    StreamEvent tableStreamEvent = iterator.next();
                    stateEvent.setStreamEvent(1, tableStreamEvent);
                    if (conditionExecutor.execute(stateEvent)) {
                        iterator.remove();
                    }
                }
            } else {
                Iterator<StreamEvent> iterator = list.iterator();
                StateEvent stateEvent = new InStateEvent(new StreamEvent[2]);
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    stateEvent.setStreamEvent(0, ((ListEvent) streamEvent).getEvent(i));
                    while (iterator.hasNext()) {
                        StreamEvent tableStreamEvent = iterator.next();
                        stateEvent.setStreamEvent(1, tableStreamEvent);
                        if (conditionExecutor.execute(stateEvent)) {
                            iterator.remove();
                        }
                    }
                }
            }
        } else {
            list.clear();
        }
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public synchronized void update(StreamEvent streamEvent, ConditionExecutor conditionExecutor, int[] attributeUpdateMappingPosition) {
        Iterator<StreamEvent> iterator = list.iterator();
        StateEvent stateEvent = new InStateEvent(new StreamEvent[2]);
        ArrayList<RemoveEvent> toAddRemoveEventList = null;
        if (enableRemoveAndAdd) {
            toAddRemoveEventList = new ArrayList<RemoveEvent>();
        }
        if (streamEvent instanceof AtomicEvent) {
            stateEvent.setStreamEvent(0, streamEvent);
            while (iterator.hasNext()) {
                StreamEvent tableStreamEvent = iterator.next();
                stateEvent.setStreamEvent(1, tableStreamEvent);
                if (conditionExecutor != null) {
                    if (conditionExecutor.execute(stateEvent)) {
                        for (int i = 0, size = attributeUpdateMappingPosition.length; i < size; i++) {
                            ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i]] = ((Event) streamEvent).getData()[i];
                        }
                        if (enableRemoveAndAdd) {
                            iterator.remove();
                            toAddRemoveEventList.add((RemoveEvent) tableStreamEvent);
                        }
                    }
                } else {
                    for (int i = 0, size = attributeUpdateMappingPosition.length; i < size; i++) {
                        ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i]] = ((Event) streamEvent).getData()[i];
                    }
                    if (enableRemoveAndAdd) {
                        iterator.remove();
                        toAddRemoveEventList.add((RemoveEvent) tableStreamEvent);
                    }
                }
            }
        } else {
            while (iterator.hasNext()) {
                StreamEvent tableStreamEvent = iterator.next();
                stateEvent.setStreamEvent(1, tableStreamEvent);
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    stateEvent.setStreamEvent(0, ((ListEvent) streamEvent).getEvent(i));
                    if (conditionExecutor != null) {
                        if (conditionExecutor.execute(stateEvent)) {
                            for (int i1 = 0, size1 = attributeUpdateMappingPosition.length; i1 < size1; i1++) {
                                ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i1]] = ((Event) streamEvent).getData()[i1];
                            }
                            if (enableRemoveAndAdd) {
                                iterator.remove();
                                toAddRemoveEventList.add((RemoveEvent) tableStreamEvent);
                            }
                        }
                    } else {
                        for (int i1 = 0, size1 = attributeUpdateMappingPosition.length; i1 < size1; i1++) {
                            ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i1]] = ((Event) streamEvent).getData()[i1];
                        }
                        if (enableRemoveAndAdd) {
                            iterator.remove();
                            toAddRemoveEventList.add((RemoveEvent) tableStreamEvent);
                        }
                    }
                }
            }
        }
        if (enableRemoveAndAdd) {
            for (RemoveEvent removeEvent : toAddRemoveEventList) {
                list.add(removeEvent);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }


    public synchronized boolean contains(AtomicEvent atomicEvent, ConditionExecutor conditionExecutor) {
        if (conditionExecutor != null) {
            Iterator<StreamEvent> iterator = list.iterator();
            StateEvent stateEvent = new InStateEvent(new StreamEvent[]{(StreamEvent) atomicEvent, null});
            while (iterator.hasNext()) {
                StreamEvent tableStreamEvent = iterator.next();
                stateEvent.setStreamEvent(1, tableStreamEvent);
                if (conditionExecutor.execute(stateEvent)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public QueryEventSource getQueryEventSource() {
        return queryEventSource;
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return list.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String SQLPredicate) {
        return list.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(StreamEvent event, ConditionExecutor conditionExecutor) {
        return list.iterator();
    }


    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public SnapshotObject snapshot() {
        return new SnapshotObject(list.currentState());
    }

    @Override
    public void restore(SnapshotObject snapshotObject) {
        list.restoreState((Object[]) snapshotObject.getData()[0]);
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
