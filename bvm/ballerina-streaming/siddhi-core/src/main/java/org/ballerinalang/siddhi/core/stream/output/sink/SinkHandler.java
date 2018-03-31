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

package org.ballerinalang.siddhi.core.stream.output.sink;

import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.util.snapshot.Snapshotable;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;

/**
 * SinkHandler is an optional interface before {@link SinkMapper}.
 * It will do optional processing to the events before sending the events to the desired mapper
 */
public abstract class SinkHandler implements Snapshotable {

    private SinkHandlerCallback sinkHandlerCallback;
    private String elementId;

    final void initSinkHandler(String elementId, StreamDefinition streamDefinition,
                               SinkHandlerCallback sinkHandlerCallback) {
        this.sinkHandlerCallback = sinkHandlerCallback;
        this.elementId = elementId;
        init(elementId, streamDefinition, sinkHandlerCallback);
    }

    public abstract void init(String elementId, StreamDefinition streamDefinition,
                              SinkHandlerCallback sinkHandlerCallback);

    public void handle(Event event) {
        handle(event, sinkHandlerCallback);
    }

    public void handle(Event[] events) {
        handle(events, sinkHandlerCallback);
    }

    public abstract void handle(Event event, SinkHandlerCallback sinkHandlerCallback);

    public abstract void handle(Event[] events, SinkHandlerCallback sinkHandlerCallback);

    public String getElementId() {
        return elementId;
    }
}
