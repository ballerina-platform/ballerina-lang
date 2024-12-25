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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import org.ballerinalang.debugadapter.breakpoint.BalBreakpoint;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.utils.ServerUtils;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.URI_SCHEME_BALA;
import static org.ballerinalang.debugadapter.utils.ServerUtils.isBalStackFrame;

/**
 * JDI Event processor implementation.
 */
public class JDIEventProcessor {

    private final ExecutionContext context;
    private final BreakpointProcessor breakpointProcessor;
    private volatile boolean isRemoteVmAttached;
    private volatile boolean interruptFlag;
    private final List<EventRequest> stepRequests = new CopyOnWriteArrayList<>();
    private static final List<ThreadReference> virtualThreads = new CopyOnWriteArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(JDIEventProcessor.class);

    private CompletableFuture<Void> listeningTask;

    JDIEventProcessor(ExecutionContext context) {
        this.context = context;
        this.breakpointProcessor = new BreakpointProcessor(context, this);
        this.isRemoteVmAttached = true;
        this.interruptFlag = false;
        this.listeningTask = null;
    }

    BreakpointProcessor getBreakpointProcessor() {
        return breakpointProcessor;
    }

    List<ThreadReference> getVirtualThreads() {
        return virtualThreads;
    }

    /**
     * Asynchronously listens and processes the incoming JDI events.
     */
    void startListenAsync() {
        // Store the future for potential cancellation
        listeningTask = CompletableFuture.runAsync(() -> {
            while (isRemoteVmAttached && !interruptFlag) {
                try {
                    EventSet eventSet = context.getDebuggeeVM().eventQueue().remove();
                    EventIterator eventIterator = eventSet.eventIterator();
                    while (eventIterator.hasNext() && isRemoteVmAttached && !interruptFlag) {
                        processEvent(eventSet, eventIterator.next());
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred while processing JDI events.", e);
                }
            }

            cleanupAfterListening();
        });
    }

    /**
     * Stops the async listening process.
     *
     * @param force if true, immediately stops listening;
     *              if false, allows current event processing to complete
     */
    private void stopListening(boolean force) {
        interruptFlag = true;
        if (Objects.nonNull(listeningTask) && !listeningTask.isDone() && force) {
            // Attempt to interrupt the task if possible
            listeningTask.cancel(true);
        }
    }

    /**
     * Performs cleanup after listening stops.
     */
    private void cleanupAfterListening() {
        if (!context.isTerminateRequestReceived() && !interruptFlag) {
            // It is not required to terminate the debuggee (remote VM) at this point, since it must be disconnected or
            // dead by now.
            context.getAdapter().terminateDebugSession(false, true);
        }
        isRemoteVmAttached = true;
        interruptFlag = false;
    }

    private void processEvent(EventSet eventSet, Event event) {
        if (event instanceof ClassPrepareEvent evt) {
            if (context.getPrevInstruction() != DebugInstruction.STEP_OVER) {
                breakpointProcessor.activateUserBreakPoints(evt.referenceType(), true);
            }
            eventSet.resume();
        } else if (event instanceof BreakpointEvent bpEvent) {
            breakpointProcessor.processBreakpointEvent(bpEvent);
        } else if (event instanceof StepEvent stepEvent) {
            ClientConfigHolder clientConfigs = context.getAdapter().getClientConfigHolder();
            int threadId = (int) stepEvent.thread().uniqueID();
            // If the debug client is in low-code mode and stepping into external sources, need to step out again
            // to reach the ballerina source.
            // TODO: Revert once the low-code mode supports rendering external sources.
            if (clientConfigs.isLowCodeMode() && hasSteppedIntoExternalSource(stepEvent)) {
                context.getAdapter().getOutputLogger().sendDebugServerOutput("Debugging external sources is not " +
                        "supported in low-code mode. Stepping-out to reach the ballerina source.");
                sendStepRequest(threadId, StepRequest.STEP_OUT);
            } else if (isBallerinaSource(stepEvent.location())) {
                notifyStopEvent(event);
            } else {
                int stepType = ((StepRequest) event.request()).depth();
                sendStepRequest(threadId, stepType);
            }
        } else if (event instanceof VMDisconnectEvent
                || event instanceof VMDeathEvent
                || event instanceof VMDisconnectedException) {
            isRemoteVmAttached = false;
        } else if (event instanceof ThreadStartEvent threadStartEvent) {
            ThreadReference thread = threadStartEvent.thread();
            if (thread.isVirtual()) {
                virtualThreads.add(thread);
            }
            eventSet.resume();
        } else if (event instanceof ThreadDeathEvent threadDeathEvent) {
            ThreadReference thread = threadDeathEvent.thread();
            virtualThreads.remove(thread);
            eventSet.resume();
        } else {
            eventSet.resume();
        }
    }

    private boolean hasSteppedIntoExternalSource(StepEvent event) {
        int stepType = ((StepRequest) event.request()).depth();
        if (stepType != StepRequest.STEP_INTO) {
            return false;
        }
        try {
            ThreadReferenceProxyImpl thread = context.getAdapter().getAllThreads().get((int) event.thread().uniqueID());
            Optional<StackFrame> topFrame = thread.frames().stream()
                    .map(this::toDapStackFrame)
                    .filter(ServerUtils::isValidFrame).findFirst();
            // If the source path of the top frame contains the URI scheme of a bala file, it can be considered as an
            // external source.
            if (topFrame.isPresent()) {
                String path = topFrame.get().getSource().getPath();
                return path.startsWith(URI_SCHEME_BALA);
            }
            return false;
        } catch (JdiProxyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Coverts a JDI stack frame instance to a DAP stack frame instance.
     */
    private StackFrame toDapStackFrame(StackFrameProxyImpl stackFrameProxy) {
        try {
            if (!isBalStackFrame(stackFrameProxy.getStackFrame())) {
                return null;
            }
            BallerinaStackFrame balStackFrame = new BallerinaStackFrame(context, 0, stackFrameProxy);
            return balStackFrame.getAsDAPStackFrame().orElse(null);
        } catch (JdiProxyException e) {
            return null;
        }
    }

    void enableBreakpoints(String qClassName, LinkedHashMap<Integer, BalBreakpoint> breakpoints) {
        breakpointProcessor.addSourceBreakpoints(qClassName, breakpoints);
        if (context.getDebuggeeVM() == null) {
            return;
        }

        // Setting breakpoints to an already running debug session.
        context.getEventManager().deleteAllBreakpoints();
        context.getDebuggeeVM().classesByName(qClassName)
                .forEach(ref -> breakpointProcessor.activateUserBreakPoints(ref, false));
    }

    void sendStepRequest(int threadId, int stepType) {
        if (stepType == StepRequest.STEP_INTO || stepType == StepRequest.STEP_OUT) {
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
                if (ServerUtils.isValidFrame(balStackFrame.getAsDAPStackFrame().get())) {
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

    public void reset() {
        stopListening(true);
        stepRequests.clear();
        virtualThreads.clear();
    }
}
