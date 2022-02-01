/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugger.test.utils;

import org.ballerinalang.debugger.test.utils.client.DAPClientConnector;

import java.util.TimerTask;

/**
 * Timer Task implementation to capture debug termination from terminated events.
 */
public class DebugTerminationListener extends TimerTask {

    DAPClientConnector connector;
    private boolean terminationFound;

    public DebugTerminationListener(DAPClientConnector connector) {
        this.connector = connector;
        this.terminationFound = false;
    }

    public boolean isTerminationFound() {
        return terminationFound;
    }

    @Override
    public void run() {
        // If the debuggee program execution is already finished, update terminationFound to true and
        // cancel the timer task immediately.
        if (!connector.getServerEventHolder().getTerminatedEvents().isEmpty() ||
            !connector.getServerEventHolder().getExitedEvents().isEmpty()) {
            terminationFound = true;
            this.cancel();
        }
    }
}
