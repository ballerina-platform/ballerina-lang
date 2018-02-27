/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.io.events;

import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.connection.DummyFuture;

import java.nio.ByteBuffer;

/**
 * Will be used to process the response once the bytes are read from the source.
 */
public class ReadBytesEvent implements Event {
    /**
     * Buffer which will be provided to the channel.
     */
    private ByteBuffer content;

    /**
     * Will be used to read bytes.
     */
    private Channel channel;

    /**
     * Will be used to notify back on the response.
     */
    private DummyFuture callback;

    public ReadBytesEvent(ByteBuffer content, Channel channel, DummyFuture callback) {
        this.content = content;
        this.callback = callback;
        this.channel = channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResponse<Integer> call() throws Exception {
        int read = channel.read(content);
        ReadByteResponse response = new ReadByteResponse(read);
        //This is the point we call the future reference
        callback.notifyBallerina();
        return response;
    }
}
