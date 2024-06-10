/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import org.ballerinalang.debugadapter.breakpoint.BalBreakpoint;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.ballerinalang.debugadapter.BreakpointProcessor.DynamicBreakpointMode;
import static org.ballerinalang.debugadapter.JBallerinaDebugServer.isBalStackFrame;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;

/**
 * JDI Event processor implementation.
 */
public class JDIEventProcessor {

    private final ExecutionContext context;
    private final BreakpointProcessor breakpointProcessor;
    private boolean isRemoteVmAttached = false;
    private final List<EventRequest> stepRequests = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(JDIEventProcessor.class);

    JDIEventProcessor(ExecutionContext context) {
        this.context = context;
        breakpointProcessor = new BreakpointProcessor(context, this);
    }

    BreakpointProcessor getBreakpointProcessor() {
        return breakpointProcessor;
    }

    List<EventRequest> getStepRequests() {
        return stepRequests;
    }

    /**
     * Asynchronously listens and processes the incoming JDI events.
     */
    void startListening() {
        CompletableFuture.runAsync(() -> {
            isRemoteVmAttached = true;
            while (isRemoteVmAttached) {
                try {
                    EventSet eventSet = context.getDebuggeeVM().eventQueue().remove();
                    EventIterator eventIterator = eventSet.eventIterator();
                    while (eventIterator.hasNext() && isRemoteVmAttached) {
                        processEvent(eventSet, eventIterator.next());
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            // Tries terminating the debug server, only if there is no any termination requests received from the
            // debug client.
            if (!context.isTerminateRequestReceived()) {
                // It is not required to terminate the debuggee (remote VM) in here, since it must be disconnected or
                // dead by now.
                context.getAdapter().terminateDebugServer(false, true);
            }
        });
    }

    private void processEvent(EventSet eventSet, Event event) {
        if (event instanceof ClassPrepareEvent evt) {
            if (context.getLastInstruction() != DebugInstruction.STEP_OVER) {
                breakpointProcessor.activateUserBreakPoints(evt.referenceType(), true);
            }
            eventSet.resume();
        } else if (event instanceof BreakpointEvent bpEvent) {
            breakpointProcessor.processBreakpointEvent(bpEvent);
        } else if (event instanceof StepEvent stepEvent) {
            int threadId = (int) stepEvent.thread().uniqueID();
            if (isBallerinaSource(stepEvent.location())) {
                notifyStopEvent(event);
            } else {
                int stepType = ((StepRequest) event.request()).depth();
                sendStepRequest(threadId, stepType);
            }
        } else if (event instanceof VMDisconnectEvent
                || event instanceof VMDeathEvent
                || event instanceof VMDisconnectedException) {
            isRemoteVmAttached = false;
        } else {
            eventSet.resume();
        }
    }

    void enableBreakpoints(String qualifiedClassName, LinkedHashMap<Integer, BalBreakpoint> breakpoints) {
        breakpointProcessor.addSourceBreakpoints(qualifiedClassName, breakpoints);

        if (context.getDebuggeeVM() != null) {
            // Setting breakpoints to a already running debug session.
            context.getEventManager().deleteAllBreakpoints();
            context.getDebuggeeVM().allClasses().forEach(referenceType ->
                    breakpointProcessor.activateUserBreakPoints(referenceType, false));
        }
    }

    void sendStepRequest(int threadId, int stepType) {
        if (stepType == StepRequest.STEP_OVER) {
            breakpointProcessor.activateDynamicBreakPoints(threadId, DynamicBreakpointMode.CURRENT);
        } else if (stepType == StepRequest.STEP_INTO || stepType == StepRequest.STEP_OUT) {
            createStepRequest(threadId, stepType);
        }
        context.getDebuggeeVM().resume();
        // Notifies the debug client that the execution is resumed.
        ContinuedEventArguments continuedEventArguments = new ContinuedEventArguments();
        continuedEventArguments.setAllThreadsContinued(true);
        context.getClient().continued(continuedEventArguments);
    }

    private void createStepRequest(int threadId, int stepType) {
        context.getEventManager().deleteEventRequests(stepRequests);
        ThreadReferenceProxyImpl proxy = context.getAdapter().getAllThreads().get(threadId);
        if (proxy == null || proxy.getThreadReference() == null) {
            return;
        }

        StepRequest request = context.getEventManager().createStepRequest(proxy.getThreadReference(),
                StepRequest.STEP_LINE, stepType);
        request.setSuspendPolicy(StepRequest.SUSPEND_ALL);
        // Todo - Replace with a class inclusive filter.
        request.addClassExclusionFilter("io.*");
        request.addClassExclusionFilter("com.*");
        request.addClassExclusionFilter("org.*");
        request.addClassExclusionFilter("java.*");
        request.addClassExclusionFilter("$lambda$main$");
        request.addCountFilter(1);
        stepRequests.add(request);
        request.enable();
    }

    /**
     * Returns the list of valid ballerina stack frames, extracted from the java stack trace.
     *
     * @param jStackFrames java stack trace.
     */
    List<BallerinaStackFrame> filterValidBallerinaFrames(List<StackFrameProxyImpl> jStackFrames) {
        List<BallerinaStackFrame> validFrames = new ArrayList<>();
        for (StackFrameProxyImpl stackFrameProxy : jStackFrames) {
            try {
                if (!isBalStackFrame(stackFrameProxy.getStackFrame())) {
                    continue;
                }
                BallerinaStackFrame balStackFrame = new BallerinaStackFrame(context, 0, stackFrameProxy);
                if (balStackFrame.getAsDAPStackFrame().isEmpty()) {
                    continue;
                }
                if (JBallerinaDebugServer.isValidFrame(balStackFrame.getAsDAPStackFrame().get())) {
                    validFrames.add(balStackFrame);
                }
            } catch (Exception ignored) {
                // it is safe to ignore JDI exceptions in here.
            }
        }
        return validFrames;
    }

    /**
     * Validates whether the given location is related to a ballerina source.
     *
     * @param location location
     * @return true if the given step event is related to a ballerina source
     */
    private boolean isBallerinaSource(Location location) {
        try {
            String sourceName = location.sourceName();
            int sourceLine = location.lineNumber();
            return sourceName.endsWith(BAL_FILE_EXT) && sourceLine > 0;
        } catch (AbsentInformationException e) {
            return false;
        }
    }

    /**
     * Notifies DAP client that the remote VM is stopped due to a breakpoint hit / step event.
     */
    void notifyStopEvent(Event event) {
        if (event instanceof BreakpointEvent breakpointEvent) {
            notifyStopEvent(StoppedEventArgumentsReason.BREAKPOINT, breakpointEvent.thread().uniqueID());
        } else if (event instanceof StepEvent stepEvent) {
            notifyStopEvent(StoppedEventArgumentsReason.STEP, stepEvent.thread().uniqueID());
        }
    }

    /**
     * Notifies DAP client that the remote VM is stopped.
     *
     * @param reason   reason to stop. (Possible values include - but not limited to those defined in
     *                 {@link StoppedEventArgumentsReason})
     * @param threadId relevant thread ID
     */
    void notifyStopEvent(String reason, long threadId) {
        context.getEventManager().deleteEventRequests(stepRequests);
        StoppedEventArguments stoppedEventArguments = new StoppedEventArguments();
        stoppedEventArguments.setReason(reason);
        stoppedEventArguments.setThreadId((int) threadId);
        stoppedEventArguments.setAllThreadsStopped(true);
        context.getClient().stopped(stoppedEventArguments);
    }
}
