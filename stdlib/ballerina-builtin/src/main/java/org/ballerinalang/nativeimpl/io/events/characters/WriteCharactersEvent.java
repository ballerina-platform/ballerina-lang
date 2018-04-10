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

package org.ballerinalang.nativeimpl.io.events.characters;

import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.Event;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.result.NumericResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Represents an event which will write characters.
 */
public class WriteCharactersEvent implements Event {
    /**
     * Channel the characters would be written.
     */
    private CharacterChannel channel;
    /**
     * The content which should be written to the channel.
     */
    private String content;
    /**
     * The starting position of the string the characters should be written.
     */
    private int offset;
    /**
     * Context of the event which will be called upon completion.
     */
    private EventContext context;

    private static final Logger log = LoggerFactory.getLogger(WriteCharactersEvent.class);

    public WriteCharactersEvent(CharacterChannel channel, String content, int offset) {
        this.channel = channel;
        this.content = content;
        this.offset = offset;
    }

    public WriteCharactersEvent(CharacterChannel channel, String content, int offset, EventContext context) {
        this.channel = channel;
        this.content = content;
        this.offset = offset;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        NumericResult result;
        try {
            int numberOfCharactersWritten = channel.write(content, offset);
            result = new NumericResult(numberOfCharactersWritten, context);
        } catch (IOException e) {
            log.error("Error occurred while writing characters", e);
            context.setError(e);
            result = new NumericResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while writing characters", e);
            context.setError(e);
            result = new NumericResult(context);
        }
        return result;
    }
}
