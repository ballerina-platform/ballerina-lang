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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.function.Function;

/**
 * Handles registration of selectable events.
 */
public class SelectableEventRegister extends Register {

    SelectableEventRegister(Event event, Function<EventResult, EventResult> function) {
        super(event, function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit() {
        throw new NotImplementedException();
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
        throw new NotImplementedException();
    }
}
