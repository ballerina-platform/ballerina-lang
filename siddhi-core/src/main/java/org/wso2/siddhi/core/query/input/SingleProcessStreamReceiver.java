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
package org.wso2.siddhi.core.query.input;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;

public class SingleProcessStreamReceiver extends ProcessStreamReceiver {

    protected ComplexEventChunk<StreamEvent> currentStreamEventChunk = new ComplexEventChunk<StreamEvent>();

    public SingleProcessStreamReceiver(String streamId) {
        super(streamId);
    }

    public SingleProcessStreamReceiver clone(String key) {
        return new SingleProcessStreamReceiver(streamId + key);
    }

    protected void processAndClear(ComplexEventChunk<StreamEvent> streamEventChunk) {

        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            streamEventChunk.remove();
            stabilizeStates();
            currentStreamEventChunk.add(streamEvent);
            next.process(currentStreamEventChunk);
            currentStreamEventChunk.clear();
        }

    }

    protected void stabilizeStates() {

    }
}
