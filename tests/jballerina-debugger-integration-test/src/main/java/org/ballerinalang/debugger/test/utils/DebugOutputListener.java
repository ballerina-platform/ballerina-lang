/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugger.test.utils;

import org.ballerinalang.debugger.test.utils.client.DAPClientConnector;
import org.eclipse.lsp4j.debug.OutputEventArguments;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Timer Task implementation to capture debugger output from server output events.
 */
public class DebugOutputListener extends TimerTask {

    private final DAPClientConnector connector;
    private OutputEventArguments lastOutputContext;
    private boolean debugOutputFound;
    private OutputEventArguments[] outputs;
    private String lastOutputLog;

    public DebugOutputListener(DAPClientConnector connector) {
        this.connector = connector;
        this.debugOutputFound = false;
        this.outputs = new OutputEventArguments[0];
    }

    public OutputEventArguments getLastOutputContext() {
        return lastOutputContext;
    }

    public boolean isDebugOutputFound() {
        return debugOutputFound;
    }

    public String getLastOutputLog() {
        return lastOutputLog;
    }

    public OutputEventArguments[] getAllDebugOutputs() {
        return outputs;
    }

    @Override
    public void run() {
        ConcurrentLinkedQueue<OutputEventArguments> events = connector.getServerEventHolder().getOutputEvents();
        while (!events.isEmpty() && connector.isConnected()) {
            OutputEventArguments[] outputEvents = events.toArray(new OutputEventArguments[0]);
            events.clear();
            if (outputEvents.length == 0) {
                continue;
            }
            this.debugOutputFound = true;
            this.outputs = outputEvents;
            this.lastOutputContext = outputEvents[outputEvents.length - 1];
            this.lastOutputLog = outputEvents[outputEvents.length - 1].getOutput();
            this.cancel();
        }
    }
}
