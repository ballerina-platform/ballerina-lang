/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.finder;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;

import java.util.Arrays;

import static org.wso2.siddhi.core.util.SiddhiConstants.*;

/**
 * Created on 12/8/14.
 */
public class SimpleFinder implements Finder {
    private FinderStateEvent event;
    private ExpressionExecutor expressionExecutor;
    private int candidateEventPosition;
    private int matchingEventPosition;
    private int size;

    public SimpleFinder(ExpressionExecutor expressionExecutor, int candidateEventPosition, int matchingEventPosition, int size) {
        this.size = size;
        this.event = new FinderStateEvent(size, 0);
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.matchingEventPosition = matchingEventPosition;
    }

    public boolean execute(StreamEvent candidateEvent) {
        event.setEvent(candidateEventPosition, candidateEvent);
        boolean result = (Boolean) expressionExecutor.execute(event);
        event.setEvent(candidateEventPosition, null);
        return result;
    }

    @Override
    public void setMatchingEvent(ComplexEvent matchingEvent) {
        if (matchingEvent instanceof StreamEvent) {
            this.event.setEvent(matchingEventPosition, ((StreamEvent) matchingEvent));
        } else {
            this.event.setEvent(((StateEvent) matchingEvent));
        }
    }

    public Finder cloneFinder() {
        return new SimpleFinder(expressionExecutor, candidateEventPosition, matchingEventPosition, size);
    }

    private class FinderStateEvent extends StateEvent {
        private StateEvent matchingStateEvent;

        public FinderStateEvent(int size, int outputSize) {
            super(size, outputSize);
        }

        public void setEvent(StateEvent matchingStateEvent) {
            this.matchingStateEvent = matchingStateEvent;
        }

        public StreamEvent getStreamEvent(int position) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                return matchingStateEvent.getStreamEvent(position);
            } else {
                return super.getStreamEvent(position);
            }
        }

        public StreamEvent[] getStreamEvents() {
            if (matchingStateEvent != null) {
                int length = matchingStateEvent.getStreamEvents().length;
                StreamEvent[] streamEvents = Arrays.copyOf(matchingStateEvent.getStreamEvents(), length + 1);
                streamEvents[length] = this.streamEvents[length];
                return streamEvents;
            } else {
                return super.getStreamEvents();
            }
        }

        public void setNext(ComplexEvent stateEvent) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setNext(stateEvent);
            } else {
                super.setNext(stateEvent);
            }
        }

        public StateEvent getNext() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getNext();
            } else {
                return super.getNext();
            }
        }

        @Override
        public void setOutputData(Object object, int index) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setOutputData(object, index);
            } else {
                super.setOutputData(object, index);
            }
        }

        @Override
        public Object getAttribute(int[] position) {
            if (position[STREAM_ATTRIBUTE_TYPE_INDEX] == STATE_OUTPUT_DATA_INDEX) {
                return outputData[position[STREAM_ATTRIBUTE_INDEX]];
            } else {
                int index = position[STREAM_EVENT_CHAIN_INDEX];
                if (matchingStateEvent != null && index < matchingStateEvent.getStreamEvents().length) {
                    return matchingStateEvent.getAttribute(position);
                } else {
                    return super.getAttribute(position);
                }
            }
        }

        public Object[] getOutputData() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getOutputData();
            } else {
                return super.getOutputData();
            }
        }

        public long getTimestamp() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getTimestamp();
            } else {
                return super.getTimestamp();
            }
        }

        public void setTimestamp(long timestamp) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setTimestamp(timestamp);
            } else {
                super.setTimestamp(timestamp);
            }
        }

        public void setType(Type type) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setType(type);
            } else {
                super.setType(type);
            }
        }

        @Override
        public Type getType() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getType();
            } else {
                return super.getType();
            }
        }

        public void setEvent(int position, StreamEvent streamEvent) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                matchingStateEvent.setEvent(position, streamEvent);
            } else {
                super.setEvent(position, streamEvent);
            }
        }

        public void addEvent(int position, StreamEvent streamEvent) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                matchingStateEvent.addEvent(position, streamEvent);
            } else {
                super.addEvent(position, streamEvent);
            }
        }

        public void removeLastEvent(int position) {
            if (matchingStateEvent != null && position < matchingStateEvent.getStreamEvents().length) {
                matchingStateEvent.removeLastEvent(position);
            } else {
                super.removeLastEvent(position);
            }
        }

        @Override
        public String toString() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.toString() + streamEvents[streamEvents.length - 1].toString();
            } else {
                return super.toString();
            }
        }

        public long getId() {
            if (matchingStateEvent != null) {
                return matchingStateEvent.getId();
            } else {
                return super.getId();
            }
        }

        public void setId(long id) {
            if (matchingStateEvent != null) {
                matchingStateEvent.setId(id);
            } else {
                super.setId(id);
            }
        }
    }
}
