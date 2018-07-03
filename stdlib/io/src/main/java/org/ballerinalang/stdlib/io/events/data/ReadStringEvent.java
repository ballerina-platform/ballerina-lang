/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.events.data;

import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.events.Event;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.EventType;
import org.ballerinalang.stdlib.io.events.result.AlphaResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ByteChannel;

/**
 * Represents the event to read string.
 */
public class ReadStringEvent implements Event {
    /**
     * Will be used to read string.
     */
    private DataChannel channel;
    /**
     * Holds context to the event.
     */
    private EventContext context;
    /**
     * Number of bytes which will represent the string.
     */
    private int nBytes;
    /**
     * Represents the char-set encoding.
     */
    private String encoding;

    private static final Logger log = LoggerFactory.getLogger(ReadStringEvent.class);

    public ReadStringEvent(DataChannel channel, EventContext context, int nBytes, String encoding) {
        this.channel = channel;
        this.context = context;
        this.nBytes = nBytes;
        this.encoding = encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        AlphaResult result;
        try {
            String alphaResult = channel.readString(nBytes, encoding);
            result = new AlphaResult(alphaResult, context);
        } catch (IOException e) {
            log.error("Error occurred while reading string", e);
            context.setError(e);
            result = new AlphaResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while reading string", e);
            context.setError(e);
            result = new AlphaResult(context);
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
    public ByteChannel getByteChannel() {
        return channel.getByteChannel();
    }
}
