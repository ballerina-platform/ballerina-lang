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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.Iterator;

/**
 * Created on 1/6/15.
 */
public class CountPreStateProcessor extends StreamPreStateProcessor {
    private final int minCount;
    private final int maxCount;
    protected volatile boolean successCondition = false;

    public CountPreStateProcessor(int minCount, int maxCount) {

        this.minCount = minCount;
        this.maxCount = maxCount;
    }


    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        System.out.println(stateId + " " + complexEventChunk);

        complexEventChunk.reset();
        StreamEvent streamEvent = (StreamEvent) complexEventChunk.next(); //Sure only one will be sent
        for (Iterator<StateEvent> iterator = pendingStateEventList.iterator(); iterator.hasNext(); ) {
            StateEvent stateEvent = iterator.next();
            if (removeIfNextStateProcessed(stateEvent, iterator, minCount + 1)) {
                continue;
            }
            if (removeIfNextStateProcessed(stateEvent, iterator, minCount + 2)) {
                continue;
            }
            process(stateEvent, streamEvent, iterator);
            if (stateChanged) {
                iterator.remove();
            }
            if (!successCondition) {
                stateEvent.setEvent(stateId, null);
            }
        }
    }

    private boolean removeIfNextStateProcessed(StateEvent stateEvent, Iterator<StateEvent> iterator, int position) {
        if (stateEvent.getStreamEvents().length > position && stateEvent.getStreamEvent(position) != null) {
            iterator.remove();
            return true;
        }
        return false;
    }

    public void successCondition() {
        this.successCondition = true;
    }
}
