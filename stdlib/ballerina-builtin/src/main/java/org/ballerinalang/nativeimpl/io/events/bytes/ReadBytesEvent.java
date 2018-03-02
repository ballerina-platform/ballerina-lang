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

package org.ballerinalang.nativeimpl.io.events.bytes;

import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.Event;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.result.NumericResult;

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
     * Context of the event which will be called upon completion.
     */
    private EventContext context;

    public ReadBytesEvent(ByteBuffer content, Channel channel) {
        this.content = content;
        this.channel = channel;
    }

    public ReadBytesEvent(ByteBuffer content, Channel channel, EventContext context) {
        this.content = content;
        this.channel = channel;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult<Integer> call() throws Exception {
        int read = channel.read(content);
        return new NumericResult(read);
    }
}
