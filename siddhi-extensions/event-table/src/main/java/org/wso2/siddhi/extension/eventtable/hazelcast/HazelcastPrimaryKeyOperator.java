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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.Operator;


/**
 * Operator which is related to Indexed Hazelcast table operations.
 */
public class HazelcastPrimaryKeyOperator implements Operator {

    protected final ExpressionExecutor expressionExecutor;
    protected final int candidateEventPosition;
    protected final int indexPosition;

    public HazelcastPrimaryKeyOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition, int indexPosition) {
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.indexPosition = indexPosition;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new HazelcastPrimaryKeyOperator(expressionExecutor.cloneExecutor(key), candidateEventPosition, indexPosition);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object candidateEvents, StreamEventCloner candidateEventCloner) {
        Object matchingKey = expressionExecutor.execute(matchingEvent);
        StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) candidateEvents).get(matchingKey);
        if (streamEvent == null) {
            return null;
        } else {
            return candidateEventCloner.copyStreamEvent(streamEvent);
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object candidateEvents) {
        Object matchingKey = expressionExecutor.execute(matchingEvent);
        StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) candidateEvents).get(matchingKey);
        return streamEvent != null;
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(deletingEvent);
            ((HazelcastPrimaryKeyEventHolder) candidateEvents).remove(matchingKey);
        }

    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object candidateEvents, UpdateAttributeMapper[] updateAttributeMappers) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(updatingEvent);
            StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) candidateEvents).get(matchingKey);
            if (streamEvent != null) {
                for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                    streamEvent.setOutputData(updateAttributeMapper.getOutputData(updatingEvent),
                            updateAttributeMapper.getCandidateAttributePosition());
                }
                ((HazelcastPrimaryKeyEventHolder) candidateEvents).replace(matchingKey, streamEvent);

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
        while (overwritingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = overwritingOrAddingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(overwritingOrAddingEvent);
            StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) candidateEvents).get(matchingKey);
            if (streamEvent != null) {
                for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                    streamEvent.setOutputData(updateAttributeMapper.getOutputData(overwritingOrAddingEvent),
                            updateAttributeMapper.getCandidateAttributePosition());
                }
                ((HazelcastPrimaryKeyEventHolder) candidateEvents).replace(matchingKey, streamEvent);
            } else {
                failedEventChunk.add(overwritingStreamEventExtractor.getOverwritingStreamEvent(overwritingOrAddingEvent));
            }
        }
        return failedEventChunk;
    }


}
