/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.events.data;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.events.Event;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.EventType;
import org.ballerinalang.stdlib.io.events.result.BooleanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Represents the event to read bool.
 */
public class ReadBoolEvent implements Event {
    /**
     * Will be used to read bool.
     */
    private DataChannel channel;
    /**
     * Holds context to the event.
     */
    private EventContext context;

    private static final Logger log = LoggerFactory.getLogger(ReadBoolEvent.class);

    public ReadBoolEvent(DataChannel channel, EventContext context) {
        this.channel = channel;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        BooleanResult result;
        try {
            boolean numericResult = channel.readBoolean();
            result = new BooleanResult(numericResult, context);
        } catch (IOException e) {
            log.error("Error occurred while reading boolean", e);
            context.setError(e);
            result = new BooleanResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while reading boolean", e);
            context.setError(e);
            result = new BooleanResult(context);
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
        return channel.getChannel();
    }

    @Override
    public boolean remaining() {
        return channel.remaining();
    }
}
