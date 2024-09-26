/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import org.ballerinalang.debugadapter.breakpoint.BalBreakpoint;
import org.ballerinalang.debugadapter.breakpoint.LogMessage;
import org.ballerinalang.debugadapter.breakpoint.TemplateLogMessage;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.DebugExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.BreakpointEventArgumentsReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ballerinalang.debugadapter.utils.PackageUtils.getQualifiedClassName;

/**
 * Implementation of Ballerina breakpoint processor. The existing implementation is capable of processing advanced
 * breakpoint types (conditional breakpoints and log-points) and, switching in-between user breakpoints and dynamic
 * breakpoints based on the debug instruction.
 *
 * @since 2201.0.1
 */
public class BreakpointProcessor {

    private final ExecutionContext context;
    private final JDIEventProcessor jdiEventProcessor;
    private final Map<String, LinkedHashMap<Integer, BalBreakpoint>> userBreakpoints = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(BreakpointProcessor.class);

    public BreakpointProcessor(ExecutionContext context, JDIEventProcessor jdiEventProcessor) {
        this.context = context;
        this.jdiEventProcessor = jdiEventProcessor;
    }

    public Map<String, LinkedHashMap<Integer, BalBreakpoint>> getUserBreakpoints() {
        return Map.copyOf(userBreakpoints);
    }

    /**
     * Updates the user breakpoints map with a list of breakpoints against its source.
     *
     * @param qualifiedClassName full-qualified classname generated for the corresponding Ballerina source file,
     *                           which contains the given breakpoints
     * @param breakpoints        the map of breakpoints against the line numbers
     */
    public void addSourceBreakpoints(String qualifiedClassName, LinkedHashMap<Integer, BalBreakpoint> breakpoints) {
        userBreakpoints.put(qualifiedClassName, breakpoints);
    }

    /**
     * Process the JDI notification of a breakpoint in the target VM. The breakpoint event is generated before the
     * code at its location is executed.
     *
     * @param bpEvent JDI breakpoint event
     */
    void processBreakpointEvent(BreakpointEvent bpEvent) {
        ReferenceType bpReference = bpEvent.location().declaringType();
        String qualifiedClassName = getQualifiedClassName(bpReference);
        Map<Integer, BalBreakpoint> fileBreakpoints = userBreakpoints.get(qualifiedClassName);
        int lineNumber = bpEvent.location().lineNumber();

        // since Ballerina code generation provides a line number of '}' in the function for the injected bytecodes, we
        // need to internally step out if we are at the last line of a function, in order to ignore having debug hits
        // on the last line.
        if (requireStepOut(bpEvent)) {
            activateDynamicBreakPoints((int) bpEvent.thread().uniqueID(), DynamicBreakpointMode.CALLER);
            context.getDebuggeeVM().resume();
        } else if (context.getLastInstruction() != null && context.getLastInstruction() != DebugInstruction.CONTINUE) {
            jdiEventProcessor.notifyStopEvent(bpEvent);
        } else if (fileBreakpoints == null || !fileBreakpoints.containsKey(lineNumber)) {
            jdiEventProcessor.notifyStopEvent(bpEvent);
        } else {
            BalBreakpoint balBreakpoint = fileBreakpoints.get(lineNumber);
            processAdvanceBreakpoints(bpEvent, balBreakpoint, lineNumber);
        }
    }

    /**
     * Examines whether the breakpoint belongs to an non-conventional breakpoint type (logpoint or conditional
     * breakpoint) and process accordingly.
     */
    private void processAdvanceBreakpoints(BreakpointEvent event, BalBreakpoint breakpoint, int lineNumber) {
        String condition = breakpoint.getCondition().isPresent() && !breakpoint.getCondition().get().isBlank() ?
                breakpoint.getCondition().get() : "";
        Optional<LogMessage> logMessage = breakpoint.getLogMessage();
        if (logMessage.isEmpty() && condition.isEmpty()) {
            jdiEventProcessor.notifyStopEvent(event);
            return;
        }

        // If there's a non-empty user defined log message and no breakpoint condition, resumes the remote VM
        // after showing the log on the debug console.
        if (logMessage.isPresent() && condition.isEmpty()) {
            printLogMessage(event, logMessage.get(), lineNumber);
            context.getDebuggeeVM().resume();
            return;
        }

        CompletableFuture<Boolean> resultFuture = evaluateBreakpointCondition(condition, event.thread(), lineNumber);
        try {
            Boolean result = resultFuture.get(5000, TimeUnit.MILLISECONDS);
            if (result) {
                if (logMessage.isPresent()) {
                    printLogMessage(event, logMessage.get(), lineNumber);
                    context.getDebuggeeVM().resume();
                } else {
                    jdiEventProcessor.notifyStopEvent(event);
                }
            } else {
                context.getDebuggeeVM().resume();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            context.getOutputLogger().sendErrorOutput(String.format("Warning: Skipping conditional breakpoint at " +
                    "line: %d, due to timeout while evaluating the condition:'%s'.", lineNumber, condition));
            if (logMessage.isPresent()) {
                printLogMessage(event, logMessage.get(), lineNumber);
                context.getDebuggeeVM().resume();
            } else {
                jdiEventProcessor.notifyStopEvent(event);
            }
        }
    }

    /**
     * Responsible for clearing dynamic(temporary) breakpoints used for step over instruction and for restoring the
     * original user breakpoints before proceeding with the other debug instructions.
     *
     * @param instruction debug instruction
     */
    void restoreUserBreakpoints(DebugInstruction instruction) {
        if (context.getDebuggeeVM() == null) {
            return;
        }

        context.getEventManager().deleteAllBreakpoints();
        if (instruction == DebugInstruction.CONTINUE || instruction == DebugInstruction.STEP_OVER) {
            context.getDebuggeeVM().allClasses().forEach(referenceType ->
                    activateUserBreakPoints(referenceType, false));
        }
    }

    /**
     * Activates user-configured source breakpoints in the program VM, via Java Debug Interface(JDI).
     *
     * @param referenceType represent the type of an object in the remote VM
     * @param shouldNotify  if true, notifies the debugger frontend with the user breakpoint verification information
     */
    void activateUserBreakPoints(ReferenceType referenceType, boolean shouldNotify) {
        try {
            // avoids setting break points if the server is running in 'no-debug' mode.
            ClientConfigHolder configHolder = context.getAdapter().getClientConfigHolder();
            if (configHolder instanceof ClientLaunchConfigHolder
                    && ((ClientLaunchConfigHolder) configHolder).isNoDebugMode()) {
                return;
            }

            String qualifiedClassName = getQualifiedClassName(referenceType);
            if (!userBreakpoints.containsKey(qualifiedClassName)) {
                return;
            }
            Map<Integer, BalBreakpoint> breakpoints = this.userBreakpoints.get(qualifiedClassName);
            for (BalBreakpoint breakpoint : breakpoints.values()) {
                List<Location> locations = referenceType.locationsOfLine(breakpoint.getLine());
                if (!locations.isEmpty()) {
                    Location loc = locations.get(0);
                    BreakpointRequest bpReq = context.getEventManager().createBreakpointRequest(loc);
                    bpReq.enable();

                    // verifies the breakpoint reachability and notifies the client if required.
                    if (!breakpoint.isVerified()) {
                        breakpoint.setVerified(true);
                        if (shouldNotify) {
                            notifyBreakPointChangesToClient(breakpoint);
                        }
                    }
                }
            }
        } catch (AbsentInformationException ignored) {
            // classes with no line number information can be ignored.
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Activates dynamic/temporary breakpoints (which will be used to process the STEP-OVER instruction) via Java Debug
     * Interface(JDI).
     *
     * @param threadId ID of the active java thread in oder to configure dynamic breakpoint on the active stack trace
     * @param mode     dynamic breakpoint mode
     */
    void activateDynamicBreakPoints(int threadId, DynamicBreakpointMode mode) {
        ThreadReferenceProxyImpl threadReference = context.getAdapter().getAllThreads().get(threadId);
        try {
            List<StackFrameProxyImpl> jStackFrames = threadReference.frames();
            List<BallerinaStackFrame> validFrames = jdiEventProcessor.filterValidBallerinaFrames(jStackFrames);

            if (mode == DynamicBreakpointMode.CURRENT && !validFrames.isEmpty()) {
                configureBreakpointsForMethod(validFrames.get(0));
            }
            // If the current function is invoked within another ballerina function, we need to explicitly set another
            // temporary breakpoint on the location of its invocation. This is supposed to handle the situations where
            // the user wants to step over on an exit point of the current function.
            if (mode == DynamicBreakpointMode.CALLER && validFrames.size() > 1) {
                configureBreakpointsForMethod(validFrames.get(1));
            }
        } catch (JdiProxyException e) {
            LOGGER.error(e.getMessage());
            int stepType = ((StepRequest) jdiEventProcessor.getStepRequests().get(0)).depth();
            jdiEventProcessor.sendStepRequest(threadId, stepType);
        }
    }

    /**
     * Configures temporary(dynamic) breakpoints for all the lines within the method, which encloses the given stack
     * frame location. This strategy is used when processing STEP_OVER requests.
     *
     * @param balStackFrame stack frame which contains the method information
     */
    private void configureBreakpointsForMethod(BallerinaStackFrame balStackFrame) {
        try {
            Location currentLocation = balStackFrame.getJStackFrame().location();
            ReferenceType referenceType = currentLocation.declaringType();
            List<Location> allLocations = currentLocation.method().allLineLocations();
            Optional<Location> firstLocation = allLocations.stream()
                    .filter(location -> location.lineNumber() > 0)
                    .min(Comparator.comparingInt(Location::lineNumber));
            Optional<Location> lastLocation = allLocations.stream().max(Comparator.comparingInt(Location::lineNumber));
            if (firstLocation.isEmpty() || lastLocation.isEmpty()) {
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
                BExpressionValue evaluatorResult = evaluateExpressionSafely(expression, threadReference);
                String condition = evaluatorResult.getStringValue();
                if (evaluatorResult.getType() != BVariableType.BOOLEAN) {
                    String errorMessage = String.format(EvaluationExceptionKind.TYPE_MISMATCH.getReason(),
                            BVariableType.BOOLEAN.getString(), evaluatorResult.getType().getString(), expression);
                    context.getOutputLogger().sendErrorOutput(String.format("Warning: Skipping conditional " +
                            "breakpoint at line: %d, due to: %s%s", lineNumber, System.lineSeparator(), errorMessage));
                }
                return condition.equalsIgnoreCase(Boolean.TRUE.toString());
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
     * Sends the logpoint message to the client's debug console.
     *
     * @param event      breakpoint event
     * @param logMessage logpoint message
     * @param lineNumber source line number of the configured logpoint
     */
    void printLogMessage(BreakpointEvent event, LogMessage logMessage, int lineNumber) {
        try {
            if (logMessage instanceof TemplateLogMessage template) {
                List<String> expressions = template.getExpressions();
                List<String> evaluationResults = new ArrayList<>();
                for (String expression : expressions) {
                    evaluationResults.add(evaluateExpressionSafely(expression, event.thread()).getStringValue());
                }
                template.resolveInterpolations(evaluationResults);
                context.getOutputLogger().sendProgramOutput(template.getMessage());
            } else {
                context.getOutputLogger().sendProgramOutput(logMessage.getMessage());
            }
        } catch (Exception e) {
            context.getOutputLogger().sendErrorOutput(String.format("Warning: Skipping logpoint at line: %d, " +
                    "due to: %s%s", lineNumber, System.lineSeparator(), e.getMessage()));
        }
    }

    private BExpressionValue evaluateExpressionSafely(String expression, ThreadReference threadReference)
            throws EvaluationException, JdiProxyException {
        // When evaluating breakpoint conditions, we might need to invoke methods in the remote JVM and it can
        // cause deadlocks if 'invokeMethod' is called from the client's event handler thread. In that case, the
        // thread will be waiting for the invokeMethod to complete and won't read the EventSet that comes in for
        // the new event. If this new EventSet is in 'SUSPEND_ALL' mode, then a deadlock will occur because no one
        // will resume the EventSet. Therefore to avoid this, we are disabling possible event requests before doing
        // the condition evaluation.
        context.getEventManager().classPrepareRequests().forEach(EventRequest::disable);
        context.getEventManager().breakpointRequests().forEach(BreakpointRequest::disable);

        ThreadReferenceProxyImpl thread = context.getAdapter().getAllThreads().get((int) threadReference.uniqueID());
        List<BallerinaStackFrame> validFrames = jdiEventProcessor.filterValidBallerinaFrames(thread.frames());
        if (validFrames.isEmpty()) {
            throw new IllegalStateException("Failed to use stack frames for evaluation");
        }

        SuspendedContext ctx = new SuspendedContext(context, thread, validFrames.get(0).getJStackFrame());
        EvaluationContext evaluationContext = new EvaluationContext(ctx);
        DebugExpressionEvaluator evaluator = new DebugExpressionEvaluator(evaluationContext);
        evaluator.setExpression(expression);
        BExpressionValue evaluationResult = evaluator.evaluate();

        // As we are disabling all the breakpoint requests before evaluating the user's conditional
        // expression, need to re-enable all the breakpoints before continuing the remote VM execution.
        restoreUserBreakpoints(context.getLastInstruction());
        return evaluationResult;
    }

    private boolean requireStepOut(BreakpointEvent event) {
        try {
            if (context.getLastInstruction() != DebugInstruction.STEP_OVER
                    && context.getLastInstruction() != DebugInstruction.STEP_OUT) {
                return false;
            }
            Location currentLocation = event.location();
            List<Location> allLocations = currentLocation.method().allLineLocations();
            Optional<Location> lastLocation = allLocations.stream().max(Comparator.comparingInt(Location::lineNumber));
            return lastLocation.isPresent() && currentLocation.lineNumber() == lastLocation.get().lineNumber();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Updates the debugger frontend with the changes made on existing user breakpoints. This method can be to verify
     * the user breakpoints which were configured before launching/connecting to the remote VM.
     *
     * @param balBreakpoint Ballerina breakpoint instance
     */
    private void notifyBreakPointChangesToClient(BalBreakpoint balBreakpoint) {
        Breakpoint dapBreakpoint = balBreakpoint.getAsDAPBreakpoint();
        BreakpointEventArguments bpEventArgs = new BreakpointEventArguments();
        bpEventArgs.setBreakpoint(dapBreakpoint);
        bpEventArgs.setReason(BreakpointEventArgumentsReason.CHANGED);
        context.getClient().breakpoint(bpEventArgs);
    }

    /**
     * Dynamic Breakpoint Options.
     */
    enum DynamicBreakpointMode {
        /**
         * Configures dynamic breakpoints only for the current method (active stack frame).
         */
        CURRENT,
        /**
         * Configures dynamic breakpoints only for the caller method (parent stack frame).
         */
        CALLER,
        /**
         * Configures dynamic breakpoints for both the current and caller methods.
         */
        BOTH
    }
}
