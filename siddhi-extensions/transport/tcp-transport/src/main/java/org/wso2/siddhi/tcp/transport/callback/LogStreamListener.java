/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.tcp.transport.callback;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class LogStreamListener implements StreamListener {
    private static final Logger log = Logger.getLogger(LogStreamListener.class);
    private StreamDefinition streamDefinition;
    private AtomicLong count = new AtomicLong(0);

    public LogStreamListener(StreamDefinition streamDefinition) {

        this.streamDefinition = streamDefinition;
    }

    @Override
    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    @Override
    public void onEvent(Event event) {
        log.info( count.incrementAndGet()+ " " + event);
    }

    @Override
    public void onEvents(Event[] events) {
        log.info(count.addAndGet(events.length) + " " + Arrays.deepToString(events));
    }
}
