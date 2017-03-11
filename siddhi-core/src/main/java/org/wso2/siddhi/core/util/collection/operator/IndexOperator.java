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

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.executor.CollectionExecutor;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class IndexOperator implements Operator {

    private CollectionExecutor collectionExecutor;

    public IndexOperator(CollectionExecutor collectionExecutor) {
        this.collectionExecutor = collectionExecutor;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        //todo check if there are any issues when not cloning
        return new IndexOperator(collectionExecutor);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object candidateEvents, StreamEventCloner candidateEventCloner) {
        return collectionExecutor.find(matchingEvent, (IndexedEventHolder) candidateEvents, candidateEventCloner);
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object candidateEvents) {
        return collectionExecutor.contains(matchingEvent, (IndexedEventHolder) candidateEvents);
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            collectionExecutor.delete(deletingEvent, (IndexedEventHolder) candidateEvents);
        }
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object candidateEvents, UpdateAttributeMapper[] updateAttributeMappers) {

        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();

            StreamEvent streamEvents = collectionExecutor.find(updatingEvent, (IndexedEventHolder) candidateEvents, null);
            if (streamEvents != null) {
                ComplexEventChunk<StreamEvent> foundEventChunk = new ComplexEventChunk<>(false);
                foundEventChunk.add(streamEvents);
                update((IndexedEventHolder) candidateEvents, updateAttributeMappers, updatingEvent, foundEventChunk);
            }
        }


    }

    @Override
    public ComplexEventChunk<StreamEvent> overwrite(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk,
                                                    Object candidateEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers,
                                                    OverwritingStreamEventExtractor overwritingStreamEventExtractor) {
        overwritingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>(overwritingOrAddingEventChunk.isBatch());

        overwritingOrAddingEventChunk.reset();
        while (overwritingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = overwritingOrAddingEventChunk.next();
            StreamEvent streamEvents = collectionExecutor.find(overwritingOrAddingEvent, (IndexedEventHolder) candidateEvents, null);
            ComplexEventChunk<StreamEvent> foundEventChunk = new ComplexEventChunk<>(false);
            foundEventChunk.add(streamEvents);
            if (foundEventChunk.getFirst() != null) {
                //for cases when indexed attribute is also updated but that not changed
                //to reduce number of passes needed to update the events
                update((IndexedEventHolder) candidateEvents, updateAttributeMappers, overwritingOrAddingEvent, foundEventChunk);
            } else {
                failedEventChunk.add(overwritingStreamEventExtractor.getOverwritingStreamEvent(overwritingOrAddingEvent));
            }
        }
        return failedEventChunk;
    }

    private void update(IndexedEventHolder candidateEvents, UpdateAttributeMapper[] updateAttributeMappers,
                        StateEvent overwritingOrAddingEvent, ComplexEventChunk<StreamEvent> foundEventChunk) {
        //for cases when indexed attribute is also updated but that not changed
        //to reduce number of passes needed to update the events
        boolean doDeleteUpdate = false;
        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
            if(doDeleteUpdate){
                break;
            }
            if (candidateEvents.isAttributeIndexed(updateAttributeMapper.getCandidateAttributePosition())) {
                //Todo how much check we need to do before falling back to Delete and then Update
                while (foundEventChunk.hasNext()) {
                    StreamEvent streamEvent = foundEventChunk.next();
                    Object updatingDate = updateAttributeMapper.getOutputData(overwritingOrAddingEvent);
                    Object candidateData = streamEvent.getOutputData()[updateAttributeMapper.getCandidateAttributePosition()];
                    if (updatingDate != null && candidateData != null && !updatingDate.equals(candidateData)) {
                        doDeleteUpdate = true;
                        break;
                    }
                }
            }
        }
        foundEventChunk.reset();
        //other cases not yet supported by Siddhi

        if (doDeleteUpdate) {
            collectionExecutor.delete(overwritingOrAddingEvent, candidateEvents);
            ComplexEventChunk<StreamEvent> toUpdateEventChunk = new ComplexEventChunk<StreamEvent>(false);
            while (foundEventChunk.hasNext()) {
                StreamEvent streamEvent = foundEventChunk.next();
                foundEventChunk.remove();
                streamEvent.setNext(null);// to make the chained state back to normal
                for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                    streamEvent.setOutputData(updateAttributeMapper.getOutputData(overwritingOrAddingEvent),
                            updateAttributeMapper.getCandidateAttributePosition());
                }
                toUpdateEventChunk.add(streamEvent);
            }
            candidateEvents.add(toUpdateEventChunk);
        } else {
            while (foundEventChunk.hasNext()) {
                StreamEvent streamEvent = foundEventChunk.next();
                streamEvent.setNext(null);// to make the chained state back to normal
                for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                    streamEvent.setOutputData(updateAttributeMapper.getOutputData(overwritingOrAddingEvent),
                            updateAttributeMapper.getCandidateAttributePosition());
                }
            }
        }
    }

}
