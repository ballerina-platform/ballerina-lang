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

package org.ballerinalang.nativeimpl.io.events.result;

import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;

/**
 * Represent alphanumeric result collection.
 */
public class AlphaCollectionResult implements EventResult<String[], EventContext> {
    /**
     * Represents a response obtained as a collection.
     */
    private String[] response;

    /**
     * Holds the context to the event.
     */
    private EventContext context;

    public AlphaCollectionResult(EventContext context) {
        this.context = context;
    }

    public AlphaCollectionResult(String[] response, EventContext context) {
        this.response = response.clone();
        this.context = context;
    }

    @Override
    public EventContext getContext() {
        return context;
    }

    @Override
    public String[] getResponse() {
        return response.clone();
    }
}
