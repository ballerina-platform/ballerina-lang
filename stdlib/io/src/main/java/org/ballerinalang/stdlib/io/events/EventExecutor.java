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

import org.ballerinalang.stdlib.io.channels.base.Channel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a task to dispatch an io event.
 */
public class EventExecutor {
    /**
     * Holds the event context of the task.
     */
    private Event evt;
    /**
     * Represent the function which should be triggered upon receiving the response.
     */
    private Function<EventResult, EventResult> function;

    EventExecutor(Event evt, Function<EventResult, EventResult> function) {
        this.evt = evt;
        this.function = function;
    }

    public int getId() {
        return evt.getChannelId();
    }

    public EventType getType() {
        return evt.getType();
    }

    public Channel getChannel() {
        return evt.getChannel();
    }

    public boolean remaining() {
        return evt.remaining();
    }

    /**
     * Executes the task once ready.
     */
    public void execute() {
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(evt);
        future.thenApply(function);
    }
}
