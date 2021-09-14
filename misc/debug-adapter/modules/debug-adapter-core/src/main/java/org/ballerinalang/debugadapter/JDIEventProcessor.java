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
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.ExpressionEvaluator;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.debugadapter.JBallerinaDebugServer.isBalStackFrame;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getQualifiedClassName;

/**
 * JDI Event processor implementation.
 */
public class JDIEventProcessor {

    private final ExecutionContext context;
    private boolean isRemoteVmAttached = false;
    private final Map<String, Map<Integer, BalBreakpoint>> breakpoints = new HashMap<>();
    private final List<EventRequest> stepEventRequests = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    private static final String CONDITION_TRUE = "true";

    JDIEventProcessor(ExecutionContext context) {
        this.context = context;
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
                context.getAdapter().terminateServer(false);
            }
        });
    }

    private void processEvent(EventSet eventSet, Event event) {
        if (event instanceof ClassPrepareEvent) {
            if (context.getLastInstruction() != DebugInstruction.STEP_OVER) {
                ClassPrepareEvent evt = (ClassPrepareEvent) event;
                configureUserBreakPoints(evt.referenceType());
            }
            eventSet.resume();
        } else if (event instanceof BreakpointEvent) {
            BreakpointEvent bpEvent = (BreakpointEvent) event;
            ReferenceType bpReference = bpEvent.location().declaringType();
            String qualifiedClassName = getQualifiedClassName(context, bpReference);
            Map<Integer, BalBreakpoint> fileBreakpoints = this.breakpoints.get(qualifiedClassName);
            int lineNumber = bpEvent.location().lineNumber();

            if (context.getLastInstruction() != null && context.getLastInstruction() != DebugInstruction.CONTINUE) {
                notifyStopEvent(event);
                return;
            } else if (fileBreakpoints == null || !fileBreakpoints.containsKey(lineNumber)) {
                notifyStopEvent(event);
                return;
            }
            BalBreakpoint balBreakpoint = fileBreakpoints.get(lineNumber);
            processBreakpointEvent(bpEvent, balBreakpoint, lineNumber);
        } else if (event instanceof StepEvent) {
            StepEvent stepEvent = (StepEvent) event;
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

    /**
     * Examines the breakpoint event to see if the related breakpoint has any user configured breakpoint condition
     * and/or log message attached with it, and process accordingly.
     */
    private void processBreakpointEvent(BreakpointEvent event, BalBreakpoint breakpoint, int lineNumber) {
        String condition = breakpoint.getCondition().isPresent() && !breakpoint.getCondition().get().isBlank() ?
                breakpoint.getCondition().get() : "";
        String logMessage = breakpoint.getLogMessage().isPresent() && !breakpoint.getLogMessage().get().isBlank() ?
                breakpoint.getLogMessage().get() : "";

        if (logMessage.isEmpty() && condition.isEmpty()) {
            notifyStopEvent(event);
            return;
        }

        // If there's a non-empty user defined log message and no breakpoint condition, resumes the remote VM
        // after showing the log on the debug console.
        if (!logMessage.isEmpty() && condition.isEmpty()) {
            context.getOutputLogger().sendProgramOutput(logMessage);
            context.getDebuggeeVM().resume();
            return;
        }

        // When evaluating breakpoint conditions, we might need to invoke methods in the remote JVM and it can
        // cause deadlocks if 'invokeMethod' is called from the client's event handler thread. In that case, the
        // thread will be waiting for the invokeMethod to complete and won't read the EventSet that comes in for
        // the new event. If this new EventSet is in 'SUSPEND_ALL' mode, then a deadlock will occur because no one
        // will resume the EventSet. Therefore to avoid this, we are disabling possible event requests before doing
        // the condition evaluation.
        context.getEventManager().classPrepareRequests().forEach(EventRequest::disable);
        context.getEventManager().breakpointRequests().forEach(BreakpointRequest::disable);
        CompletableFuture<Boolean> resultFuture = evaluateBreakpointCondition(condition, event.thread(), lineNumber);
        try {
            Boolean result = resultFuture.get(5000, TimeUnit.MILLISECONDS);
            if (result) {
                if (!logMessage.isEmpty()) {
                    context.getOutputLogger().sendProgramOutput(logMessage);
                    // As we are disabling all the breakpoint requests before evaluating the user's conditional
                    // expression, need to re-enable all the breakpoints before continuing the remote VM execution.
                    restoreBreakpoints(context.getLastInstruction());
                    context.getDebuggeeVM().resume();
                } else {
                    notifyStopEvent(event);
                }
            } else {
                // As we are disabling all the breakpoint requests before evaluating the user's conditional expression,
                // need to re-enable all the breakpoints before continuing the remote VM execution.
                restoreBreakpoints(context.getLastInstruction());
                context.getDebuggeeVM().resume();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            context.getOutputLogger().sendErrorOutput(String.format("Warning: Skipping conditional breakpoint at " +
                    "line: %d, due to timeout while evaluating the condition:'%s'.", lineNumber, condition));
            if (!logMessage.isEmpty()) {
                context.getOutputLogger().sendProgramOutput(logMessage);
                restoreBreakpoints(context.getLastInstruction());
                context.getDebuggeeVM().resume();
            } else {
                notifyStopEvent(event);
            }
        }
    }

    void setBreakpoints(String debugSourcePath, Map<Integer, BalBreakpoint> breakpoints) {
        Optional<String> qualifiedClassName = getQualifiedClassName(context, debugSourcePath);
        qualifiedClassName.ifPresent(s -> this.breakpoints.put(s, breakpoints));

        if (context.getDebuggeeVM() != null) {
            // Setting breakpoints to a already running debug session.
            context.getEventManager().deleteAllBreakpoints();
            context.getDebuggeeVM().allClasses().forEach(this::configureUserBreakPoints);
        }
    }

    void sendStepRequest(int threadId, int stepType) {
        if (stepType == StepRequest.STEP_OVER) {
            configureDynamicBreakPoints(threadId);
        } else if (stepType == StepRequest.STEP_INTO || stepType == StepRequest.STEP_OUT) {
            createStepRequest(threadId, stepType);
        }
        context.getDebuggeeVM().resume();
        // Notifies the debug client that the execution is resumed.
        ContinuedEventArguments continuedEventArguments = new ContinuedEventArguments();
        continuedEventArguments.setAllThreadsContinued(true);
        context.getClient().continued(continuedEventArguments);
    }

    void restoreBreakpoints(DebugInstruction instruction) {
        if (context.getDebuggeeVM() == null) {
            return;
        }

        context.getEventManager().deleteAllBreakpoints();
        if (instruction == DebugInstruction.CONTINUE || instruction == DebugInstruction.STEP_OVER) {
            context.getDebuggeeVM().allClasses().forEach(this::configureUserBreakPoints);
        }
    }

    private void configureUserBreakPoints(ReferenceType referenceType) {
        try {
            // Avoids setting break points if the server is running in 'no-debug' mode.
            ClientConfigHolder configHolder = context.getAdapter().getClientConfigHolder();
            if (configHolder instanceof ClientLaunchConfigHolder
                    && ((ClientLaunchConfigHolder) configHolder).isNoDebugMode()) {
                return;
            }

            String qualifiedClassName = getQualifiedClassName(context, referenceType);
            if (!breakpoints.containsKey(qualifiedClassName)) {
                return;
            }
            Map<Integer, BalBreakpoint> breakpoints = this.breakpoints.get(qualifiedClassName);
            for (BalBreakpoint bp : breakpoints.values()) {
                List<Location> locations = referenceType.locationsOfLine(bp.getLine());
                if (!locations.isEmpty()) {
                    Location loc = locations.get(0);
                    BreakpointRequest bpReq = context.getEventManager().createBreakpointRequest(loc);
                    bpReq.enable();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void configureDynamicBreakPoints(int threadId) {
        ThreadReferenceProxyImpl threadReference = context.getAdapter().getAllThreads().get(threadId);
        try {
            List<StackFrameProxyImpl> jStackFrames = threadReference.frames();
            List<BallerinaStackFrame> validFrames = filterValidBallerinaFrames(jStackFrames);

            if (!validFrames.isEmpty()) {
                configureBreakpointsForMethod(validFrames.get(0));
            }
            // If the current function is invoked within another ballerina function, we need to explicitly set another
            // temporary breakpoint on the location of its invocation. This is supposed to handle the situations where
            // the user wants to step over on an exit point of the current function.
            if (validFrames.size() > 1) {
                configureBreakpointsForMethod(validFrames.get(1));
            }
        } catch (JdiProxyException e) {
            LOGGER.error(e.getMessage());
            int stepType = ((StepRequest) this.stepEventRequests.get(0)).depth();
            sendStepRequest(threadId, stepType);
        }
    }

    /**
     * Configures temporary(dynamic) breakpoints for all the lines within the method, which encloses the given stack
     * frame location. This strategy is used when processing STEP_OVER requests.
     */
    private void configureBreakpointsForMethod(BallerinaStackFrame balStackFrame) {
        try {
            Location currentLocation = balStackFrame.getJStackFrame().location();
            ReferenceType referenceType = currentLocation.declaringType();
            List<Location> allLocations = currentLocation.method().allLineLocations();
            Optional<Location> firstLocation = allLocations.stream().min(Comparator.comparingInt(Location::lineNumber));
            Optional<Location> lastLocation = allLocations.stream().max(Comparator.comparingInt(Location::lineNumber));
            if (firstLocation.isEmpty()) {
                return;
            }

            int nextStepPoint = firstLocation.get().lineNumber();
            do {
                List<Location> locations = referenceType.locationsOfLine(nextStepPoint);
                if (!locations.isEmpty() && (locations.get(0).lineNumber() > firstLocation.get().lineNumber())) {
                    // Checks whether there are any user breakpoint configured for the same location, before adding the
                    // dynamic breakpoint.
                    boolean bpAlreadyExist = context.getEventManager().breakpointRequests().stream()
                            .anyMatch(breakpointRequest -> breakpointRequest.location().equals(locations.get(0)));
                    if (!bpAlreadyExist) {
                        BreakpointRequest bpReq = context.getEventManager().createBreakpointRequest(locations.get(0));
                        bpReq.enable();
                    }
                }
                nextStepPoint++;
            } while (nextStepPoint <= lastLocation.get().lineNumber());
        } catch (AbsentInformationException | JdiProxyException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void createStepRequest(int threadId, int stepType) {
        context.getEventManager().deleteEventRequests(stepEventRequests);
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
        stepEventRequests.add(request);
        request.addCountFilter(1);
        stepEventRequests.add(request);
        request.enable();
    }

    /**
     * Evaluates the given breakpoint condition (expression) using the ballerina debugger expression evaluation engine.
     *
     * @param expression      breakpoint expression
     * @param threadReference suspended thread reference, which should be used to get the top stack frame
     * @return result of the given breakpoint condition (logical expression).
     */
    private CompletableFuture<Boolean> evaluateBreakpointCondition(String expression, ThreadReference threadReference,
                                                                   int lineNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ThreadReferenceProxyImpl thread = context.getAdapter().getAllThreads()
                        .get((int) threadReference.uniqueID());
                List<BallerinaStackFrame> validFrames = filterValidBallerinaFrames(thread.frames());
                if (validFrames.isEmpty()) {
                    return false;
                }

                SuspendedContext ctx = new SuspendedContext(context, thread, validFrames.get(0).getJStackFrame());
                ExpressionEvaluator evaluator = new ExpressionEvaluator(ctx);
                Value evaluatorResult = evaluator.evaluate(expression);
                String condition = VariableFactory.getVariable(ctx, evaluatorResult).getDapVariable().getValue();
                return condition.equalsIgnoreCase(CONDITION_TRUE);
            } catch (EvaluationException e) {
                context.getOutputLogger().sendErrorOutput(String.format("Warning: Skipping conditional breakpoint " +
                        "at line: %d, due to: %s%s", lineNumber, System.lineSeparator(), e.getMessage()));
                return false;
            } catch (Exception e) {
                context.getOutputLogger().sendErrorOutput(String.format("Warning: Skipping conditional breakpoint " +
                        "at line: %d, due to an internal error", lineNumber));
                return false;
            }
        });
    }

    /**
     * Returns the list of valid ballerina stack frames, extracted from the java stack trace.
     *
     * @param jStackFrames java stack trace.
     */
    private List<BallerinaStackFrame> filterValidBallerinaFrames(List<StackFrameProxyImpl> jStackFrames) {
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
    private void notifyStopEvent(Event event) {
        context.getEventManager().deleteEventRequests(stepEventRequests);
        StoppedEventArguments stoppedEventArguments = new StoppedEventArguments();

        if (event instanceof BreakpointEvent) {
            stoppedEventArguments.setReason(StoppedEventArgumentsReason.BREAKPOINT);
            stoppedEventArguments.setThreadId((int) ((BreakpointEvent) event).thread().uniqueID());
        } else if (event instanceof StepEvent) {
            stoppedEventArguments.setReason(StoppedEventArgumentsReason.STEP);
            stoppedEventArguments.setThreadId((int) ((StepEvent) event).thread().uniqueID());
        }

        stoppedEventArguments.setAllThreadsStopped(true);
        context.getClient().stopped(stoppedEventArguments);
    }
}
