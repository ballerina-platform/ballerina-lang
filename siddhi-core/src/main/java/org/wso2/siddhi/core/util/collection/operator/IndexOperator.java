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

package org.wso2.siddhi.core.util.collection.operator;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.executor.CollectionExecutor;

import java.util.HashSet;
import java.util.Set;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class IndexOperator implements Operator {

    private static final Logger log = Logger.getLogger(IndexOperator.class);
    protected String queryName;
    private CollectionExecutor collectionExecutor;

    public IndexOperator(CollectionExecutor collectionExecutor, String queryName) {
        this.collectionExecutor = collectionExecutor;
        this.queryName = queryName;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        //todo check if there are any issues when not cloning
        return new IndexOperator(collectionExecutor, queryName);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object storeEvents, StreamEventCloner storeEventCloner) {
        return collectionExecutor.find(matchingEvent, (IndexedEventHolder) storeEvents, storeEventCloner);
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object storeEvents) {
        return collectionExecutor.contains(matchingEvent, (IndexedEventHolder) storeEvents);
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            collectionExecutor.delete(deletingEvent, (IndexedEventHolder) storeEvents);
        }
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents, UpdateAttributeMapper[]
            updateAttributeMappers) {

        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();

            StreamEvent streamEvents = collectionExecutor.find(updatingEvent, (IndexedEventHolder) storeEvents, null);
            if (streamEvents != null) {
                ComplexEventChunk<StreamEvent> foundEventChunk = new ComplexEventChunk<>(false);
                foundEventChunk.add(streamEvents);
                update((IndexedEventHolder) storeEvents, updateAttributeMappers, updatingEvent, foundEventChunk);
            }
        }


    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk,
                                                    Object storeEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers,
                                                    AddingStreamEventExtractor addingStreamEventExtractor) {
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>
                (updatingOrAddingEventChunk.isBatch());
        updatingOrAddingEventChunk.reset();
        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = updatingOrAddingEventChunk.next();
            StreamEvent streamEvents = collectionExecutor.find(overwritingOrAddingEvent, (IndexedEventHolder)
                    storeEvents, null);
            ComplexEventChunk<StreamEvent> foundEventChunk = new ComplexEventChunk<>(false);
            foundEventChunk.add(streamEvents);
            if (foundEventChunk.getFirst() != null) {
                //for cases when indexed attribute is also updated but that not changed
                //to reduce number of passes needed to update the events
                update((IndexedEventHolder) storeEvents, updateAttributeMappers, overwritingOrAddingEvent,
                        foundEventChunk);
            } else {
                failedEventChunk.add(addingStreamEventExtractor.getAddingStreamEvent(overwritingOrAddingEvent));
            }
        }
        return failedEventChunk;
    }

    private void update(IndexedEventHolder storeEvents, UpdateAttributeMapper[] updateAttributeMappers,
                        StateEvent overwritingOrAddingEvent, ComplexEventChunk<StreamEvent> foundEventChunk) {
        //for cases when indexed attribute is also updated but that not changed
        //to reduce number of passes needed to update the events
        boolean doDeleteUpdate = false;
        boolean fail = false;
        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
            if (doDeleteUpdate || fail) {
                break;
            }
            if (storeEvents.isAttributeIndexed(updateAttributeMapper.getStoreEventAttributePosition())) {
                //Todo how much check we need to do before falling back to Delete and then Update
                foundEventChunk.reset();
                Set<Object> keys = null;
                if (updateAttributeMapper.getStoreEventAttributePosition() == storeEvents
                        .getPrimaryKeyAttributePosition()) {
                    keys = new HashSet<Object>(storeEvents.getAllPrimaryKeys());
                }
                while (foundEventChunk.hasNext()) {
                    StreamEvent streamEvent = foundEventChunk.next();
                    Object updatingDate = updateAttributeMapper.getUpdateEventOutputData(overwritingOrAddingEvent);
                    Object storeEventData = streamEvent.getOutputData()[updateAttributeMapper
                            .getStoreEventAttributePosition()];
                    if (updatingDate != null && storeEventData != null && !updatingDate.equals(storeEventData)) {
                        doDeleteUpdate = true;
                        if (keys != null) {
                            keys.remove(storeEventData);
                            if (!keys.add(updatingDate)) {
                                log.error("Update failed for event :" + overwritingOrAddingEvent + ", as there is " +
                                        "already an event stored with primary key '" +
                                        updatingDate + "' at '" + queryName + "'");
                                fail = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        foundEventChunk.reset();
        if (!fail) {
            //other cases not yet supported by Siddhi

            if (doDeleteUpdate) {
                collectionExecutor.delete(overwritingOrAddingEvent, storeEvents);
                ComplexEventChunk<StreamEvent> toUpdateEventChunk = new ComplexEventChunk<StreamEvent>(false);
                while (foundEventChunk.hasNext()) {
                    StreamEvent streamEvent = foundEventChunk.next();
                    foundEventChunk.remove();
                    streamEvent.setNext(null); // to make the chained state back to normal
                    for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                        updateAttributeMapper.mapOutputData(overwritingOrAddingEvent, streamEvent);
                    }
                    toUpdateEventChunk.add(streamEvent);
                }
                storeEvents.add(toUpdateEventChunk);
            } else {
                while (foundEventChunk.hasNext()) {
                    StreamEvent streamEvent = foundEventChunk.next();
                    streamEvent.setNext(null); // to make the chained state back to normal
                    for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                        updateAttributeMapper.mapOutputData(overwritingOrAddingEvent, streamEvent);
                    }
                }
            }
        }
    }

}
