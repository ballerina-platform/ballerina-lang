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

package org.ballerinalang.stdlib.io.events;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the context of the event. This will hold Ballerina context and the callback.
 */
public class EventContext {
    /**
     * Holds the context which will be used to set the relevant results.
     */
    private Context context;
    /**
     * Callback which will be triggered upon completion.
     */
    private CallableUnitCallback callback;
    /**
     * Represents any error which will be thrown.
     */
    private Throwable error;
    /**
     * Represents the register which will hold the context of the event.
     */
    private Register register;
    /**
     * Will hold the list of properties related to the context.
     */
    private Map<String, Object> properties = new HashMap<>();

    public EventContext() {
    }

    public EventContext(Context context) {
        this.context = context;
    }

    public EventContext(Context context, CallableUnitCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public CallableUnitCallback getCallback() {
        return callback;
    }

    public Register getRegister() {
        return register;
    }

    public Context getContext() {
        return context;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
