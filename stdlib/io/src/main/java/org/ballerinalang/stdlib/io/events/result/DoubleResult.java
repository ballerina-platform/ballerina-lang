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

package org.ballerinalang.stdlib.io.events.result;

import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventResult;

/**
 * Represents the result which will be returned as double.
 */
public class DoubleResult implements EventResult<Double, EventContext> {
    /**
     * Will represent a value.
     * <p>
     * This could be numberOfBytes, numberOfCharacters.
     */
    private double value;

    /**
     * Holds the context to the event.
     */
    private EventContext context;

    public DoubleResult(EventContext context) {
        this.context = context;
    }

    public DoubleResult(double count, EventContext context) {
        this.value = count;
        this.context = context;
    }

    @Override
    public EventContext getContext() {
        return context;
    }

    @Override
    public Double getResponse() {
        return value;
    }
}
