/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.util.collection.operator;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;

import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.ANY;

/**
 * Created on 12/8/14.
 */
public class SimpleIndexedOperator implements Operator {
    private ExpressionExecutor expressionExecutor;
    private final long withinTime;
    private int matchingEventPosition;

    public SimpleIndexedOperator(ExpressionExecutor expressionExecutor, int matchingEventPosition, long withinTime) {

        this.expressionExecutor = expressionExecutor;
        this.matchingEventPosition = matchingEventPosition;
        this.withinTime = withinTime;
    }

    @Override
    public Finder cloneFinder() {
        return new SimpleIndexedOperator(expressionExecutor, matchingEventPosition, withinTime);
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {
        StreamEvent matchingStreamEvent;
        if (matchingEvent instanceof StreamEvent) {
            matchingStreamEvent = ((StreamEvent) matchingEvent);
        } else {
            matchingStreamEvent = ((StateEvent) matchingEvent).getStreamEvent(matchingEventPosition);
        }
        Object matchingKey = expressionExecutor.execute(matchingStreamEvent);
        if (candidateEvents instanceof Map) {
            StreamEvent streamEvent = ((Map<Object, StreamEvent>) candidateEvents).get(matchingKey);
            if (streamEvent == null) {
                return null;
            } else {
                if (withinTime != ANY) {
                    long timeDifference = Math.abs(matchingStreamEvent.getTimestamp() - streamEvent.getTimestamp());
                    if (timeDifference > withinTime) {
                        return null;
                    }
                }

                return streamEventCloner.copyStreamEvent(streamEvent);
            }
        } else {
            throw new OperationNotSupportedException(SimpleIndexedOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
        }

    }

    @Override
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StreamEvent deletingEvent = deletingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(deletingEvent);
            if (candidateEvents instanceof Map) {
                if (withinTime != ANY) {
                    StreamEvent streamEvent = ((Map<Object, StreamEvent>) candidateEvents).get(matchingKey);
                    if (streamEvent != null) {
                        long timeDifference = Math.abs(deletingEvent.getTimestamp() - streamEvent.getTimestamp());
                        if (timeDifference > withinTime) {
                            return;
                        }
                    }
                }
                ((Map<Object, StreamEvent>) candidateEvents).remove(matchingKey);
            } else {
                throw new OperationNotSupportedException(SimpleIndexedOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        }
    }

    @Override
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StreamEvent updatingEvent = updatingEventChunk.next();

            Object matchingKey = expressionExecutor.execute(updatingEvent);
            if (candidateEvents instanceof Map) {
                StreamEvent streamEvent = ((Map<Object, StreamEvent>) candidateEvents).get(matchingKey);
                if (streamEvent != null) {
                    if (withinTime != ANY) {
                        long timeDifference = Math.abs(updatingEvent.getTimestamp() - streamEvent.getTimestamp());
                        if (timeDifference > withinTime) {
                            return;
                        }
                    }

                    for (int i = 0, size = mappingPosition.length; i < size; i++) {
                        streamEvent.setOutputData(updatingEvent.getOutputData()[i], mappingPosition[i]);
                    }
                }
            } else {
                throw new OperationNotSupportedException(SimpleIndexedOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
            }

        }
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {
        StreamEvent matchingStreamEvent;
        if (matchingEvent instanceof StreamEvent) {
            matchingStreamEvent = ((StreamEvent) matchingEvent);
        } else {
            matchingStreamEvent = ((StateEvent) matchingEvent).getStreamEvent(matchingEventPosition);
        }
        Object matchingKey = expressionExecutor.execute(matchingStreamEvent);
        if (candidateEvents instanceof Map) {
            StreamEvent streamEvent = ((Map<Object, StreamEvent>) candidateEvents).get(matchingKey);
            if (streamEvent != null) {
                if (withinTime != ANY) {
                    long timeDifference = Math.abs(matchingStreamEvent.getTimestamp() - streamEvent.getTimestamp());
                    if (timeDifference > withinTime) {
                        return false;
                    }
                }

                return true;
            }
            return false;
        } else {
            throw new OperationNotSupportedException(SimpleIndexedOperator.class.getCanonicalName() + " does not support " + candidateEvents.getClass().getCanonicalName());
        }

    }
}
