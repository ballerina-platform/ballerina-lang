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

import java.util.function.Function;

/**
 * Factory which will identify the event which should be registered.
 */
public class EventRegister {
    /**
     * Represents the factory instance.
     */
    private static EventRegister factory = new EventRegister();

    private EventRegister() {
    }

    public static EventRegister getFactory() {
        return factory;
    }

    /**
     * Returns the register which corresponds to the channel.
     *
     * @param isSelectable specifies whether the given channel is selectable.
     * @param event        the event of execution.
     * @param function     the callback function.
     * @return the register.
     */
    public Register register(int id, boolean isSelectable, Event event, Function<EventResult, EventResult> function) {
        if (isSelectable) {
            return new SelectableEventRegister(id, event, function);
        } else {
            return new InstantEventRegister(id, event, function);
        }
    }
}
