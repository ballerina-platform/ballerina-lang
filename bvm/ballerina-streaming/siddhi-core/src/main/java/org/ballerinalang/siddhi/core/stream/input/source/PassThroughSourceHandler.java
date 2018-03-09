/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.stream.input.source;

import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;

/**
 * Implementation of InputEventHandlerCallback used when no SourceHandler is used.
 */
public class PassThroughSourceHandler implements InputEventHandlerCallback {

    private InputHandler inputHandler;

    PassThroughSourceHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        inputHandler.send(event);
    }

    @Override
    public void sendEvents(Event[] events) throws InterruptedException {
        inputHandler.send(events);
    }
}
