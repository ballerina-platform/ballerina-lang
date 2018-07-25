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
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

    public ReadBytesEvent(Channel channel, byte[] content, EventContext context) {
        this.content = ByteBuffer.wrap(content);
        this.context = context;
        this.channel = channel;
    }

    /**
     * <p>
     * Return an array which contains read dataExpr.
     * </p>
     * <p>
     * If the buffer is not filled will shrink the array to fit for what's filled
     * </p>
     *
     * @return array which contains dataExpr read from the channel.
     */
    private byte[] getContentData() {
        int bufferSize = content.limit();
        int readPosition = content.position();
        byte[] content = this.content.array();
        final int startPosition = 0;
        if (readPosition == bufferSize) {
            return content;
        }
        return Arrays.copyOfRange(content, startPosition, readPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        NumericResult result;
        try {
            if (channel.hasReachedEnd()) {
                if (log.isDebugEnabled()) {
                    log.debug("Channel " + channel.hashCode() + " reached it's end");
                }
                context.setError(new Throwable(IOConstants.IO_EOF));
                result = new NumericResult(context);
            } else {
                int numberOfBytesRead = channel.read(content);
                byte[] content = getContentData();
                context.getProperties().put(CONTENT_PROPERTY, content);
                result = new NumericResult(numberOfBytesRead, context);
            }
        } catch (IOException e) {
            log.error("Error occurred while reading bytes", e);
            context.setError(e);
            result = new NumericResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while reading bytes", e);
            context.setError(e);
            result = new NumericResult(context);
        }
        return result;
    }

    @Override
    public int getChannelId() {
        return channel.id();
    }

    @Override
    public boolean isSelectable() {
        return channel.isSelectable();
    }

    @Override
    public EventType getType() {
        return EventType.READ;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
