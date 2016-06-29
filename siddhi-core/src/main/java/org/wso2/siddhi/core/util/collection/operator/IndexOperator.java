/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    public Finder cloneFinder(String key) {
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

            StreamEvent streamEvent = collectionExecutor.find(updatingEvent, (IndexedEventHolder) candidateEvents, null);
            if (streamEvent != null) {
                ComplexEventChunk<StreamEvent> resultEventChunk = new ComplexEventChunk<StreamEvent>(false);
                resultEventChunk.add(streamEvent);
                collectionExecutor.delete(updatingEvent, (IndexedEventHolder) candidateEvents);

                while (resultEventChunk.hasNext()) {
                    StreamEvent resultEvent = resultEventChunk.next();
                    for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                        resultEvent.setOutputData(updateAttributeMapper.getOutputData(updatingEvent),
                                updateAttributeMapper.getCandidateAttributePosition());
                    }
                }
                ((IndexedEventHolder) candidateEvents).add(resultEventChunk);
            }

        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> overwriteOrAdd(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk,
                                                         Object candidateEvents,
                                                         UpdateAttributeMapper[] updateAttributeMappers,
                                                         OverwritingStreamEventExtractor overwritingStreamEventExtractor) {
        overwritingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>(overwritingOrAddingEventChunk.isBatch());

        overwritingOrAddingEventChunk.reset();
        while (overwritingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = overwritingOrAddingEventChunk.next();
            StreamEvent streamEvent = collectionExecutor.find(overwritingOrAddingEvent, (IndexedEventHolder) candidateEvents, null);
            if (streamEvent != null) {
                ComplexEventChunk<StreamEvent> resultEventChunk = new ComplexEventChunk<StreamEvent>(false);
                resultEventChunk.add(streamEvent);
                collectionExecutor.delete(overwritingOrAddingEvent, (IndexedEventHolder) candidateEvents);

                while (resultEventChunk.hasNext()) {
                    StreamEvent resultEvent = resultEventChunk.next();
                    for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                        resultEvent.setOutputData(updateAttributeMapper.getOutputData(overwritingOrAddingEvent),
                                updateAttributeMapper.getCandidateAttributePosition());
                    }
                }
                ((IndexedEventHolder) candidateEvents).add(resultEventChunk);
            } else {
                failedEventChunk.add(overwritingStreamEventExtractor.getOverwritingStreamEvent(overwritingOrAddingEvent));
            }
        }
        return failedEventChunk;
    }

}
