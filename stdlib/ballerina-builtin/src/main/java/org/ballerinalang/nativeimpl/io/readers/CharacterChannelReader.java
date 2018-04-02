/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.nativeimpl.io.readers;

import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.characters.ReadCharactersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This sub class of {@link Reader} use to convert {@link CharacterChannel} to Reader instance.
 */
public class CharacterChannelReader extends Reader {

    private static final Logger log = LoggerFactory.getLogger(CharacterChannelReader.class);

    private CharacterChannel channel;

    public CharacterChannelReader(CharacterChannel channel) {
        this.channel = channel;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Read offset: " + off + " length: " + len);
        }
        ReadCharactersEvent event = new ReadCharactersEvent(channel, len);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        final EventResult eventResult;
        try {
            eventResult = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
        final String response = (String) eventResult.getResponse();
        if (response != null && !response.isEmpty()) {
            final char[] chars = response.toCharArray();
            System.arraycopy(chars, 0, cbuf, off, chars.length);
            if (log.isDebugEnabled()) {
                log.debug("No of characters read: " + chars.length);
            }
            return chars.length;
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
