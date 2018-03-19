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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Will be used to process the response once the bytes are read from the source.
 */
public class ReadBytesEvent implements Event {
    /**
     * Holds the name of the property which will hold a reference to the byte content.
     */
    public static final String CONTENT_PROPERTY = "byte_content";
    /**
     * Buffer which will be provided to the channel.
     */
    private ByteBuffer content;
    /**
     * Will be used to read bytes.
     */
    private Channel channel;
    /**
     * Holds context to the event.
     */
    private EventContext context;

    private static final Logger log = LoggerFactory.getLogger(ReadBytesEvent.class);

    public ReadBytesEvent(Channel channel, byte[] content, int offset, EventContext context) {
        this.content = ByteBuffer.wrap(content);
        this.context = context;
        this.channel = channel;
        this.content.position(offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        NumericResult result;
        try {
            int numberOfBytesRead = channel.read(content);
            byte[] content = this.content.array();
            context.getProperties().put(CONTENT_PROPERTY, content);
            result = new NumericResult(numberOfBytesRead, context);
        } catch (IOException e) {
            log.error("Error occurred while reading bytes", e);
            context.setError(e);
            result = new NumericResult(context);
        }
        return result;
    }
}
