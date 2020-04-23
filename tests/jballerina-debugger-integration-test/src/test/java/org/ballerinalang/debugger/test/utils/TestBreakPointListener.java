/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.debugger.test.utils.client.TestDAPClientConnector;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Timer Task implementation to capture breakpoints from server stop events.
 */
public class TestBreakPointListener extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestBreakPointListener.class);
    private final TestDAPClientConnector connector;
    private final List<BallerinaTestBreakPoint> capturedBreakpoints;

    public TestBreakPointListener(TestDAPClientConnector connector) {
        this.connector = connector;
        this.capturedBreakpoints = new ArrayList<>();
    }

    public List<BallerinaTestBreakPoint> getCapturedBreakpoints() {
        return capturedBreakpoints;
    }

    @Override
    public void run() {
        ConcurrentLinkedQueue<StoppedEventArguments> events = connector.getServerEventHolder().getStoppedEvents();
        while (!events.isEmpty() && connector.isConnected()) {
            StoppedEventArguments event = events.poll();
            if (event == null || !event.getReason().equals(StoppedEventArgumentsReason.BREAKPOINT)) {
                continue;
            }
            BallerinaTestBreakPoint bp = fetchBreakPoint(event);
            if (bp != null) {
                capturedBreakpoints.add(bp);
                resumeProgram(event);
            }
        }

        // If the debuggee program execution is already finished, cancels the timer task immediately.
        if (!connector.getServerEventHolder().getTerminatedEvents().isEmpty() ||
                !connector.getServerEventHolder().getExitedEvents().isEmpty()) {
            this.cancel();
        }
    }

    private void resumeProgram(StoppedEventArguments args) {
        ContinueArguments continueArgs = new ContinueArguments();
        continueArgs.setThreadId(args.getThreadId());
        try {
            connector.getRequestManager().resume(continueArgs);
        } catch (Exception e) {
            LOGGER.warn("continue request failed.", e);
        }
    }


    private BallerinaTestBreakPoint fetchBreakPoint(StoppedEventArguments args) {

        if (!connector.isConnected()) {
            return null;
        }
        StackTraceArguments stackTraceArgs = new StackTraceArguments();
        stackTraceArgs.setThreadId(args.getThreadId());
        try {
            ThreadsResponse threadsResp = connector.getRequestManager().threads();
            if (Arrays.stream(threadsResp.getThreads()).noneMatch(t -> t.getId().equals(args.getThreadId()))) {
                return null;
            }
            StackTraceResponse stackTraceResp = connector.getRequestManager().
                    stackTrace(stackTraceArgs);
            StackFrame[] stackFrames = stackTraceResp.getStackFrames();
            if (stackFrames.length == 0) {
                return null;
            }
            return new BallerinaTestBreakPoint(stackFrames[0].getSource().getPath(), stackFrames[0].getLine());
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching stack frames", e);
            return null;
        }
    }
}
