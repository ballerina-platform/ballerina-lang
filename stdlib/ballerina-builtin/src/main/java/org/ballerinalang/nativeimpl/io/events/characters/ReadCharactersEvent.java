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
import org.ballerinalang.nativeimpl.io.events.result.AlphaResult;

/**
 * Represents an event which will read characters.
 */
public class ReadCharactersEvent implements Event {
    /**
     * Represents the character channel the characters will be read from.
     */
    private CharacterChannel channel;

    /**
     * Specified the number of characters which should be read.
     */
    private int numberOfCharacters;

    /**
     * Context of the event which will be called upon completion.
     */
    private EventContext context;

    public ReadCharactersEvent(CharacterChannel channel, int numberOfCharacters) {
        this.channel = channel;
        this.numberOfCharacters = numberOfCharacters;
    }

    public ReadCharactersEvent(CharacterChannel channel, int numberOfCharacters, EventContext context) {
        this.channel = channel;
        this.numberOfCharacters = numberOfCharacters;
        this.context = context;
    }

    @Override
    public EventResult call() throws Exception {
        String content = channel.read(numberOfCharacters);
        return new AlphaResult(content);
    }
}
