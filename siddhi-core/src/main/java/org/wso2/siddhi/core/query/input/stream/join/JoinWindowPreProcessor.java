/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.query.input.stream.join;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

/**
 * This JoinProcessor always act as a pre processor and pass the expired events to the next processor without blocking them.
 * Since it is intended to used join {@link org.wso2.siddhi.core.table.WindowEventTable}, the next processor
 * {@link org.wso2.siddhi.core.query.processor.stream.window.WindowWindowProcessor} will receive the expired events and pass
 * them to the post {@link JoinProcessor}. The post {@link JoinProcessor} will process the expired events as usual.
 */
public class JoinWindowPreProcessor extends JoinProcessor {

    /**
     * Construct a JoinWindowPreProcessor object.
     *
     * @param leftJoinProcessor  is this processor on left side
     * @param outerJoinProcessor is this processor used for outer join
     */
    public JoinWindowPreProcessor(boolean leftJoinProcessor, boolean outerJoinProcessor, int matchingStreamIndex) {
        super(leftJoinProcessor, true, outerJoinProcessor, matchingStreamIndex);
    }

    /**
     * Process all the events same as {@link JoinProcessor#process(ComplexEventChunk)} except for expired events.
     * This method passes the expired events to the next processor without blocking them.
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        if (trigger) {
            ComplexEventChunk<StateEvent> returnEventChunk = new ComplexEventChunk<StateEvent>(true);
            StateEvent joinStateEvent = new StateEvent(2, 0);
            StreamEvent nextEvent = (StreamEvent) complexEventChunk.getFirst();
            complexEventChunk.clear();
            while (nextEvent != null) {

                StreamEvent streamEvent = nextEvent;
                nextEvent = streamEvent.getNext();
                streamEvent.setNext(null);
                joinLock.lock();
                try {
                    ComplexEvent.Type eventType = streamEvent.getType();
                    if (eventType == ComplexEvent.Type.EXPIRED) {
                        // Pass both timer and expired events to the next processor
                        complexEventChunk.add(streamEvent);
                        nextProcessor.process(complexEventChunk);
                        complexEventChunk.clear();
                        continue;
                    } else if (eventType == ComplexEvent.Type.RESET || eventType == ComplexEvent.Type.TIMER) {
                        continue;
                    }
                    joinStateEvent.setEvent(matchingStreamIndex, streamEvent);
                    StreamEvent foundStreamEvent = findableProcessor.find(joinStateEvent, finder);
                    joinStateEvent.setEvent(matchingStreamIndex, null);
                    if (foundStreamEvent == null) {
                        if (outerJoinProcessor && !leftJoinProcessor) {
                            returnEventChunk.add(joinEventBuilder(null, streamEvent));
                        } else if (outerJoinProcessor && leftJoinProcessor) {
                            returnEventChunk.add(joinEventBuilder(streamEvent, null));
                        }
                    } else {
                        while (foundStreamEvent != null) {
                            if (!leftJoinProcessor) {
                                returnEventChunk.add(joinEventBuilder(foundStreamEvent, streamEvent));
                            } else {
                                returnEventChunk.add(joinEventBuilder(streamEvent, foundStreamEvent));
                            }
                            foundStreamEvent = foundStreamEvent.getNext();
                        }
                    }
                    complexEventChunk.add(streamEvent);
                    nextProcessor.process(complexEventChunk);
                    complexEventChunk.clear();

                } finally {
                    joinLock.unlock();
                }
                if (returnEventChunk.getFirst() != null) {
                    selector.process(returnEventChunk);
                    returnEventChunk.clear();
                }

            }
        } else {
            joinLock.lock();
            try {
                nextProcessor.process(complexEventChunk);
            } finally {
                joinLock.unlock();
            }

        }
    }
}
