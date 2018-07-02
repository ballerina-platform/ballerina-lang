/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.events;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a task to dispatch an io event.
 */
public class EventExecutor {
    /**
     * Unique identifier to co-relate between the channel and io events (read, write).
     */
    private int id;
    /**
     * Holds the event context of the task.
     */
    private Event evt;
    /**
     * Represent the function which should be triggered upon receiving the response.
     */
    private Function<EventResult, EventResult> function;

    public EventExecutor(int id, Event evt, Function<EventResult, EventResult> function) {
        this.id = id;
        this.evt = evt;
        this.function = function;
    }

    public int getId() {
        return id;
    }

    /**
     * Executes the task once ready.
     */
    public void execute() {
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(evt);
        future.thenApply(function);
    }
}
