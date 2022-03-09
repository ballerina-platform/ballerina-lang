/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.BreakpointEventArgumentsReason;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Timer Task implementation to capture breakpoint events.
 */
public class BreakpointEventListener extends TimerTask {

    private final DAPClientConnector connector;
    private final List<BallerinaTestDebugPoint> modifiedBreakpoints;
    private boolean breakpointEventFound;

    public BreakpointEventListener(DAPClientConnector connector) {
        this.connector = connector;
        this.breakpointEventFound = false;
        this.modifiedBreakpoints = new ArrayList<>();
    }

    public boolean isBreakpointEventFound() {
        return breakpointEventFound;
    }

    public List<BallerinaTestDebugPoint> getModifiedBreakpoints() {
        return modifiedBreakpoints;
    }

    @Override
    public void run() {
        ConcurrentLinkedQueue<BreakpointEventArguments> events = connector.getServerEventHolder().getBreakpointEvents();
        while (!events.isEmpty() && connector.isConnected()) {
            breakpointEventFound = true;
            events.forEach(event -> {
                if (event != null && event.getReason().equals(BreakpointEventArgumentsReason.CHANGED)) {
                    Breakpoint breakpoint = event.getBreakpoint();
                    modifiedBreakpoints.add(new BallerinaTestDebugPoint(Path.of(breakpoint.getSource().getPath()),
                            breakpoint.getLine(), breakpoint.isVerified()));
                }
            });
            events.clear();
            this.cancel();
        }

        // If the debuggee program execution is already finished, cancels the timer task immediately.
        if (!connector.getServerEventHolder().getTerminatedEvents().isEmpty() ||
                !connector.getServerEventHolder().getExitedEvents().isEmpty()) {
            this.cancel();
        }
    }
}
