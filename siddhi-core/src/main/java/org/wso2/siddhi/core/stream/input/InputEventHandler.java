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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * This class wraps {@link InputHandler} class in order to guarantee exactly once processing
 */
public class InputEventHandler implements Snapshotable {

    private static final Logger log = Logger.getLogger(InputEventHandler.class);
    private InputHandler inputHandler;
    private String elementId;
    /**
     * {@link Event#id} of the last event which is processed by this mapper
     */
    private Long lastEventId = null;
    private final Semaphore mutex;

    public InputEventHandler(InputHandler inputHandler, ExecutionPlanContext executionPlanContext) {
        this.inputHandler = inputHandler;
        this.elementId = executionPlanContext.getElementIdGenerator().createNewId();
        executionPlanContext.getSnapshotService().addSnapshotable(inputHandler.hashCode() + "inputeventhandler", this);
        mutex = new Semaphore(1);
    }

    public void sendEvent(Event event) throws InterruptedException {
        long eventId = event.getId();
        // event id -1 is reserved for the events that are arriving for the first Siddhi node
        try {
            mutex.acquire();
            if (lastEventId == null || eventId == -1 || lastEventId < eventId) {
                lastEventId = eventId;
                inputHandler.send(event);
            }
        } finally {
            mutex.release();
        }
    }

    public void sendEvents(Event[] events) throws InterruptedException {
        List<Event> eventsToBeSent = new ArrayList<>();
        for (Event event : events) {
            long eventId = event.getId();
            // event id -1 is reserved for the events that are arriving for the first Siddhi node
            if (lastEventId == null || eventId == -1 || lastEventId < eventId) {
                lastEventId = eventId;
                eventsToBeSent.add(event);
            }
        }
        inputHandler.send(eventsToBeSent.toArray(new Event[eventsToBeSent.size()]));
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        try {
            mutex.acquire();
            state.put("LastEventId", lastEventId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error when getting the current state", e);
        } finally {
            mutex.release();
        }
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        lastEventId = (Long) state.get("LastEventId");
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
