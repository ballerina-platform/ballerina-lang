/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.listener.http2;

import io.netty.util.internal.PlatformDependent;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the streams related to a single server channel.
 */
public class Http2ServerChannel {
    // streamIdRequestMap contains the mapping of http carbon messages vs stream id to support multiplexing
    private Map<Integer, InboundMessageHolder> streamIdRequestMap = PlatformDependent.newConcurrentHashMap();
    private Map<String, Http2DataEventListener> dataEventListeners;

    Http2ServerChannel() {
        dataEventListeners = new HashMap<>();
    }

    void destroy() {
        streamIdRequestMap.clear();
    }

    public Map<Integer, InboundMessageHolder> getStreamIdRequestMap() {
        return streamIdRequestMap;
    }

    InboundMessageHolder getInboundMessage(int streamId) {
        return streamIdRequestMap.get(streamId);
    }

    /**
     * Adds a listener which listen for HTTP/2 data events.
     *
     * @param name              name of the listener
     * @param dataEventListener the data event listener
     */
    void addDataEventListener(String name, Http2DataEventListener dataEventListener) {
        dataEventListeners.put(name, dataEventListener);
    }

    /**
     * Gets the list of listeners which listen for HTTP/2 data events.
     *
     * @return list of data event listeners
     */
    public List<Http2DataEventListener> getDataEventListeners() {
        return new ArrayList<>(dataEventListeners.values());
    }
}
