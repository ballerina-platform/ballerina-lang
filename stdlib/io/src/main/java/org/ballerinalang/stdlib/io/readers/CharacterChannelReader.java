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

package org.ballerinalang.stdlib.io.readers;

import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventManager;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.characters.ReadCharactersEvent;
import org.ballerinalang.stdlib.io.events.result.AlphaResult;
import org.ballerinalang.stdlib.io.utils.IOConstants;
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

    private EventContext context;

    public CharacterChannelReader(CharacterChannel channel, EventContext context) {
        this.channel = channel;
        this.context = context;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Read offset: " + off + " length: " + len);
        }
        ReadCharactersEvent event = new ReadCharactersEvent(channel, len, context);
        CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
        final AlphaResult eventResult;
        try {
            eventResult = (AlphaResult) future.get();
            Throwable error = eventResult.getContext().getError();
            if (null != error && !IOConstants.IO_EOF.equals(error.getMessage())) {
                throw new IOException(error);
            }
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
