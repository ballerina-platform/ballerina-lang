/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.stream.input;

import org.wso2.siddhi.core.event.Event;

/**
 * This class wraps {@link InputHandler} class in order to guarantee exactly once processing
 */
public class InputEventHandler {

    private InputHandler inputHandler;

    public InputEventHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public Long sendEvent(Event event, Long lastEventId) throws InterruptedException {
        long eventId = event.getId();
        // event id -1 is reserved for the events that are arriving for the first Siddhi node
        if (lastEventId == null || eventId == -1 || lastEventId < eventId) {
            inputHandler.send(event);
            return eventId;
        }
        return lastEventId;
    }

    public Long sendEvents(Event[] events, Long lastEventId) throws InterruptedException {
        for (Event event : events) {
            long eventId = event.getId();
            // event id -1 is reserved for the events that are arriving for the first Siddhi node
            if (lastEventId == null || eventId == -1 || lastEventId < eventId) {
                lastEventId = eventId;
            }
        }
        inputHandler.send(events);
        return lastEventId;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }
}
