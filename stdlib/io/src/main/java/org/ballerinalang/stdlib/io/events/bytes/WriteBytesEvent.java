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

package org.ballerinalang.stdlib.io.events.bytes;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.events.Event;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.EventType;
import org.ballerinalang.stdlib.io.events.result.NumericResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents an event which will write bytes.
 */
public class WriteBytesEvent implements Event {
    /**
     * Channel the bytes will be written.
     */
    private Channel byteChannel;
    /**
     * The reference to the content which should be written.
     */
    private ByteBuffer writeBuffer;
    /**
     * Holds the context to the event.
     */
    private EventContext context;

    private static final Logger log = LoggerFactory.getLogger(WriteBytesEvent.class);

    public WriteBytesEvent(Channel byteChannel, byte[] content, int startOffset, EventContext context) {
        this.byteChannel = byteChannel;
        writeBuffer = ByteBuffer.wrap(content);
        //If a larger position is set, the position would be disregarded
        writeBuffer.position(startOffset);
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        NumericResult result;
        try {
            int numberOfBytesWritten = byteChannel.write(writeBuffer);
            result = new NumericResult(numberOfBytesWritten, context);
        } catch (IOException e) {
            log.error("Error occurred while reading bytes", e);
            context.setError(e);
            result = new NumericResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while writing bytes", e);
            context.setError(e);
            result = new NumericResult(context);
        }
        return result;
    }

    @Override
    public int getChannelId() {
        return byteChannel.id();
    }

    @Override
    public boolean isSelectable() {
        return byteChannel.isSelectable();
    }

    @Override
    public EventType getType() {
        return EventType.WRITE;
    }

    @Override
    public Channel getChannel() {
        return byteChannel;
    }

    @Override
    public boolean remaining() {
        return byteChannel.remaining();
    }
}
