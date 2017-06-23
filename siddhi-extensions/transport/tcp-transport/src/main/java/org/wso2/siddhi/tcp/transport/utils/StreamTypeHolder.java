/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.tcp.transport.utils;


import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event stream data type holder
 */
public class StreamTypeHolder {
    private Map<String, StreamInfo> streamInfoMap = new ConcurrentHashMap<String, StreamInfo>();

    public StreamInfo getStreamInfo(String streamId) {
        return streamInfoMap.get(streamId);
    }

    public void putStreamCallback(StreamListener streamListener) {
        if (this.streamInfoMap.containsKey(streamListener.getStreamDefinition().getId())) {
            throw new SiddhiAppCreationException("TCP source with name '" + streamListener.getStreamDefinition()
                    .getId() + "' already defined !");
        }
        this.streamInfoMap.put(streamListener.getStreamDefinition().getId(), new StreamInfo(streamListener));
    }

    public void removeStreamCallback(String streamId) {
        this.streamInfoMap.remove(streamId);
    }

    public int getNoOfRegisteredStreamListeners() {
        return streamInfoMap.size();
    }
}
