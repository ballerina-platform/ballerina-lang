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

package org.ballerinalang.stdlib.io.events;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.socket.server.SelectorManager;
import org.ballerinalang.stdlib.io.socket.server.SocketIOExecutorQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

/**
 * Handles registration of selectable events.
 */
public class SelectableEventRegister extends Register {

    private static final Logger log = LoggerFactory.getLogger(SelectorManager.class);

    SelectableEventRegister(Event event, Function<EventResult, EventResult> function) {
        super(event, function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit() {
        switch (exec.getType()) {
            case READ:
                SocketIOExecutorQueue.getInstance().registerRead(exec.getId(), exec);
                break;
            case WRITE:
                exec.execute();
                break;
            case CLOSE:
                discard();
                break;
        }
    }

    /**
     * <p>
     * This method will inform the relevant selector to discard the channel information.
     * </p>
     * <p>
     * This will be triggered at an event whether the channel returns a read error, reaching eof
     * </p>
     * {@inheritDoc}
     */
    @Override
    public void discard() {
        Channel channel = exec.getChannel();
        SocketChannel socketChannel = (SocketChannel) channel.getByteChannel();
        try {
            final SelectionKey selectionKey = socketChannel.keyFor(SelectorManager.getInstance());
            selectionKey.cancel();
        } catch (IOException e) {
            log.error("Unable to deregister selection key for channel[" + channel + "]: " + e.getMessage(), e);
        }
    }
}
