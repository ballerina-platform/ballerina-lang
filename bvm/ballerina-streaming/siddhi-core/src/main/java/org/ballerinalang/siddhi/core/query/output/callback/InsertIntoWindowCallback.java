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

package org.ballerinalang.siddhi.core.query.output.callback;

import org.ballerinalang.siddhi.core.debugger.SiddhiDebugger;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;

/**
 * This callback is an adapter between {@link org.ballerinalang.siddhi.core.stream.StreamJunction} and {@link Window}.
 * It receives {@link ComplexEventChunk}s and insert them into {@link Window}.
 */
public class InsertIntoWindowCallback extends OutputCallback {
    /**
     * Window to which the events have to be inserted.
     */
    private final Window window;

    /**
     * StreamDefinition of the window.
     */
    private final StreamDefinition outputStreamDefinition;

    public InsertIntoWindowCallback(Window window, StreamDefinition outputStreamDefinition, String queryName) {
        super(queryName);
        this.window = window;
        this.outputStreamDefinition = outputStreamDefinition;
    }

    /**
     * Add the event into the {@link Window}.
     *
     * @param complexEventChunk the event to add
     * @param noOfEvents        number of events
     */
    @Override
    public void send(ComplexEventChunk complexEventChunk, int noOfEvents) {
        if (getSiddhiDebugger() != null) {
            getSiddhiDebugger()
                    .checkBreakPoint(getQueryName(), SiddhiDebugger.QueryTerminal.OUT, complexEventChunk.getFirst());
        }
        // If events are inserted directly from another window, expired events can arrive
        complexEventChunk.reset();
        while (complexEventChunk.hasNext()) {
            ComplexEvent complexEvent = complexEventChunk.next();
            if (complexEvent.getType() == ComplexEvent.Type.EXPIRED) {
                complexEvent.setType(ComplexEvent.Type.CURRENT);
            }
        }
        window.add(complexEventChunk);
    }

    /**
     * Return the StreamDefinition of the corresponding window.
     *
     * @return stream definition of the target window
     */
    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

    /**
     * Return the {@link Window} associated with this callback.
     *
     * @return the Window
     */
    public Window getWindow() {
        return window;
    }
}
