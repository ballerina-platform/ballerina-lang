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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.ANY;

// TODO : check the usage of abs() when calculating time frame
public class HazelcastOperator implements Operator {
    private final ZeroStreamEventConverter streamEventConverter;
    private final StreamEvent matchingEvent;
    protected ExpressionExecutor expressionExecutor;
    protected int candidateEventPosition;
    protected int matchingEventPosition;
    protected int streamEventSize;
    protected long withinTime;
    private FinderStateEvent event;
    private int matchingEventOutputSize;

    public HazelcastOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition, int matchingEventPosition,
                             int streamEventSize, long withinTime, int matchingEventOutputSize) {
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.matchingEventPosition = matchingEventPosition;
        this.streamEventSize = streamEventSize;
        this.withinTime = withinTime;
        this.matchingEventOutputSize = matchingEventOutputSize;
        this.event = new FinderStateEvent(streamEventSize, 0);
        this.matchingEvent = new StreamEvent(0, 0, matchingEventOutputSize);
        this.streamEventConverter = new ZeroStreamEventConverter();
    }

    private boolean execute(StreamEvent candidateEvent) {
        event.setEvent(candidateEventPosition, candidateEvent);
        boolean result = (Boolean) expressionExecutor.execute(event);
        event.setEvent(candidateEventPosition, null);
        return result;
    }

    @Override
    public Finder cloneFinder() {
        return new HazelcastOperator(expressionExecutor, candidateEventPosition, matchingEventPosition, streamEventSize,
                withinTime, matchingEventOutputSize);
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {
        try {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, (StreamEvent) matchingEvent);
            } else {
                this.event.setEvent((StateEvent) matchingEvent);
            }
            if (candidateEvents instanceof ComplexEventChunk) {
                return findInComplexEventChunk((ComplexEventChunk) candidateEvents, streamEventCloner);
            } else if (candidateEvents instanceof Map) {
                return findInCollection(((Map) candidateEvents).values(), streamEventCloner);
            } else if (candidateEvents instanceof Collection) {
                return findInCollection((Collection) candidateEvents, streamEventCloner);
            } else {
                throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                        " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        } finally {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, null);
            } else {
                this.event.setEvent(null);
            }
        }
    }

    private StreamEvent findInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk, StreamEventCloner streamEventCloner) {
        candidateEventChunk.reset();
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }

    protected StreamEvent findInCollection(Collection<StreamEvent> candidateEvents, StreamEventCloner streamEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        return returnEventChunk.getFirst();
    }

    @Override
    public void delete(ComplexEventChunk deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            ComplexEvent deletingEvent = deletingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(deletingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);

                if (candidateEvents instanceof ComplexEventChunk) {
                    deleteInComplexEventChunk((ComplexEventChunk) candidateEvents);
                } else if (candidateEvents instanceof Map) {
                    deleteInMap((Map) candidateEvents);
                } else if (candidateEvents instanceof Collection) {
                    deleteInCollection((Collection) candidateEvents);
                } else {
                    throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                            " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    private void deleteInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                candidateEventChunk.remove();
            }
        }
    }

    public void deleteInMap(Map<Object, StreamEvent> candidateEvents) {
        for (Map.Entry<Object, StreamEvent> entry : candidateEvents.entrySet()) {
            StreamEvent streamEvent = entry.getValue();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                candidateEvents.remove(entry.getKey());
            }
        }
    }

    private void deleteInCollection(Collection<StreamEvent> candidateEvents) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                candidateEvents.remove(streamEvent);
            }
        }
    }

    @Override
    public void update(ComplexEventChunk updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            ComplexEvent updatingEvent = updatingEventChunk.next();
            try {
                streamEventConverter.convertStreamEvent(updatingEvent, matchingEvent);
                this.event.setEvent(matchingEventPosition, matchingEvent);
                if (candidateEvents instanceof ComplexEventChunk) {
                    updateInComplexEventChunk((ComplexEventChunk) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof List) {
                    updateInList((List) candidateEvents, mappingPosition);
                } else if (candidateEvents instanceof Map) {
                    updateInCollection(((Map) candidateEvents).values(), mappingPosition);
                } else if (candidateEvents instanceof Collection) {
                    updateInCollection((Collection) candidateEvents, mappingPosition);
                } else {
                    throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                            " does not support " + candidateEvents.getClass().getCanonicalName());
                }
            } finally {
                this.event.setEvent(matchingEventPosition, null);
            }
        }
    }

    private void updateInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk, int[] mappingPosition) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }

    private void updateInCollection(Collection<StreamEvent> candidateEvents, int[] mappingPosition) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
            }
        }
    }

    private void updateInList(List<StreamEvent> candidateEvents, int[] mappingPosition) {
        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                int streamEventIndex = candidateEvents.indexOf(streamEvent);
                for (int i = 0, size = mappingPosition.length; i < size; i++) {
                    streamEvent.setOutputData(event.getStreamEvent(matchingEventPosition).getOutputData()[i], mappingPosition[i]);
                }
                candidateEvents.set(streamEventIndex, streamEvent);
            }
        }
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {
        try {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, ((StreamEvent) matchingEvent));
            } else {
                this.event.setEvent(((StateEvent) matchingEvent));
            }
            if (candidateEvents instanceof ComplexEventChunk) {
                return containsInComplexEventChunk((ComplexEventChunk) candidateEvents);
            } else if (candidateEvents instanceof Map) {
                return containsInCollection(((Map) candidateEvents).values());
            } else if (candidateEvents instanceof Collection) {
                return containsInCollection((Collection) candidateEvents);
            } else {
                throw new OperationNotSupportedException(HazelcastOperator.class.getCanonicalName() +
                        " does not support " + candidateEvents.getClass().getCanonicalName());
            }
        } finally {
            if (matchingEvent instanceof StreamEvent) {
                this.event.setEvent(matchingEventPosition, null);
            } else {
                this.event.setEvent(null);
            }
        }
    }

    private boolean containsInCollection(Collection<StreamEvent> candidateEvents) {

        for (StreamEvent streamEvent : candidateEvents) {
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsInComplexEventChunk(ComplexEventChunk<StreamEvent> candidateEventChunk) {
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent streamEvent = candidateEventChunk.next();
            if (withinTime != ANY) {
                long timeDifference = Math.abs(event.getStreamEvent(matchingEventPosition).getTimestamp() - streamEvent.getTimestamp());
                if (timeDifference > withinTime) {
                    continue;
                }
            }
            if (execute(streamEvent)) {
                return true;
            }
        }
        return false;
    }

    protected class FinderStateEvent extends StateEvent {

        public FinderStateEvent(int size, int outputSize) {
            super(size, outputSize);
        }

        public void setEvent(StateEvent matchingStateEvent) {
            if (matchingStateEvent != null) {
                System.arraycopy(matchingStateEvent.getStreamEvents(), 0, streamEvents, 0, matchingStateEvent.getStreamEvents().length);
            } else {
                for (int i = 0; i < streamEvents.length - 1; i++) {
                    streamEvents[i] = null;
                }
            }
        }
    }
}
