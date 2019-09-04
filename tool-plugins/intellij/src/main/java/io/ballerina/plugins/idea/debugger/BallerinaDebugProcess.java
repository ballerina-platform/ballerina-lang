/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.debugger;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import io.ballerina.plugins.idea.debugger.breakpoint.BallerinaBreakPointType;
import io.ballerina.plugins.idea.debugger.breakpoint.BallerinaBreakpointProperties;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ballerina debug process which handles debugging.
 */
public class BallerinaDebugProcess extends XDebugProcess {

    private static final Logger LOGGER = Logger.getInstance(BallerinaDebugProcess.class);

    private final XDebugSession debugSession;
    private final ProcessHandler processHandler;
    private final ExecutionConsole executionConsole;
    private final BallerinaDebuggerEditorsProvider editorsProvider;
    private final BallerinaBreakpointHandler breakpointHandler;
    private final BallerinaDAPClientConnector dapClientConnector;
    private boolean isConnected = false;
    private boolean isRemoteDebugMode = false;
    private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();

    private static final int MAX_RETRY_COUNT = 6;

    public BallerinaDebugProcess(@NotNull XDebugSession session, @NotNull BallerinaDAPClientConnector connector,
                                 @Nullable ExecutionResult executionResult) {
        super(session);
        debugSession = session;
        dapClientConnector = connector;
        processHandler = executionResult == null ? super.getProcessHandler() : executionResult.getProcessHandler();
        executionConsole = executionResult == null ? super.createConsole() : executionResult.getExecutionConsole();
        editorsProvider = new BallerinaDebuggerEditorsProvider();
        breakpointHandler = new BallerinaBreakpointHandler();
        if (executionResult == null) {
            isRemoteDebugMode = true;
        }
    }

    public BallerinaDAPClientConnector getDapClientConnector() {
        return dapClientConnector;
    }

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return processHandler;
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        return executionConsole;
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[]{breakpointHandler};
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void sessionInitialized() {
        final int[] retryAttempt = {0};
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            getSession().getConsoleView().print("Connecting to the debug server...\n",
                    ConsoleViewContentType.SYSTEM_OUTPUT);
            // If already connected with the debug server, tries to set breakpoints and attach with the remote jvm.
            if (dapClientConnector.isConnected()) {
                LOGGER.debug("Connection is already created.");
                isConnected = true;
                startDebugSession();
                return;
            }

            // Else, tries to initiate the socket connection.
            while (!isConnected && (++retryAttempt[0] <= MAX_RETRY_COUNT)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (!dapClientConnector.isConnected()) {
                    LOGGER.debug("Not connected. Retrying...");
                    dapClientConnector.createConnection();
                    if (dapClientConnector.isConnected()) {
                        isConnected = true;
                        if (isRemoteDebugMode) {
                            getSession().getConsoleView().print("Connected to the remote server at " +
                                    dapClientConnector.getAddress() + ".\n", ConsoleViewContentType.SYSTEM_OUTPUT);
                        }
                        LOGGER.debug("Connection created.");
                        startDebugSession();
                        break;
                    }
                } else {
                    LOGGER.debug("Connection is already created.");
                    isConnected = true;
                    startDebugSession();
                    break;
                }
            }
            if (!dapClientConnector.isConnected()) {
                getSession().getConsoleView().print("Connection to debug server at " +
                                dapClientConnector.getAddress() + " could not be established.\n",
                        ConsoleViewContentType.ERROR_OUTPUT);
                getSession().stop();
            }
        });
    }

    private void startDebugSession() {
        if (!isConnected) {
            return;
        }
        initBreakpointHandlersAndSetBreakpoints();
        LOGGER.debug("Sending breakpoints.");
        breakpointHandler.sendBreakpoints(breakpoints, true);
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        Long workerID = getWorkerID(context);
        if (workerID == null || !checkCanPerformCommands()) {
            return;
        }
        NextArguments nextArgs = new NextArguments();
        nextArgs.setThreadId(workerID);
        try {
            dapClientConnector.getRequestManager().next(nextArgs);
        } catch (Exception e) {
            LOGGER.warn("Step over request failed", e);
        }
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        Long workerID = getWorkerID(context);
        if (workerID == null || !checkCanPerformCommands()) {
            return;
        }
        StepInArguments stepInArgs = new StepInArguments();
        stepInArgs.setThreadId(workerID);
        try {
            dapClientConnector.getRequestManager().stepIn(stepInArgs);
        } catch (Exception e) {
            LOGGER.warn("Step in request failed", e);
        }
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        Long workerID = getWorkerID(context);
        if (workerID == null || !checkCanPerformCommands()) {
            return;
        }
        StepOutArguments stepOutArgs = new StepOutArguments();
        stepOutArgs.setThreadId(workerID);
        try {
            dapClientConnector.getRequestManager().stepOut(stepOutArgs);
        } catch (Exception e) {
            LOGGER.warn("Step out request failed", e);
        }
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        Long workerID = getWorkerID(context);
        if (workerID == null || !checkCanPerformCommands()) {
            return;
        }
        ContinueArguments continueArgs = new ContinueArguments();
        continueArgs.setThreadId(workerID);
        try {
            dapClientConnector.getRequestManager().resume(continueArgs);
        } catch (Exception e) {
            LOGGER.warn("Step out request failed", e);
        }
    }

    // Todo - Evaluate the previous logic (suspend context based) and reuse if this impl interrupts other processes.
    @Override
    public void stop() {
        // If we don't call this using the executeOnPooledThread(), the UI will hang until the debug server is stopped.
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!isConnected) {
                return;
            }
            try {
                dapClientConnector.disconnectFromServer();
                getSession().getConsoleView().print("Disconnected Successfully from the debug server.\n",
                        ConsoleViewContentType.SYSTEM_OUTPUT);
            } catch (Exception e) {
                getSession().getConsoleView().print("Disconnected Exceptionally from the debug server.\n",
                        ConsoleViewContentType.SYSTEM_OUTPUT);
            } finally {
                XDebugSession session = getSession();
                if (session != null) {
                    session.stop();
                }
                isConnected = false;
            }
        });

    }

    @Nullable
    private Long getWorkerID(@Nullable XSuspendContext context) {
        if (context != null) {
            XExecutionStack activeExecutionStack = context.getActiveExecutionStack();
            if (activeExecutionStack instanceof BallerinaSuspendContext.BallerinaExecutionStack) {
                return ((BallerinaSuspendContext.BallerinaExecutionStack) activeExecutionStack).getMyWorkerID();
            }
        }
        getSession().getConsoleView().print("Error occurred while getting the thread ID.",
                ConsoleViewContentType.ERROR_OUTPUT);
        getSession().stop();
        return null;
    }

    @Nullable
    @Override
    public XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
        return super.getSmartStepIntoHandler();
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        // Todo
    }

    @Override
    public boolean checkCanInitBreakpoints() {
        // We manually initialize the breakpoints after connecting to the debug server.
        return false;
    }

    public void handleDebugHit(StoppedEventArguments args) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!isConnected) {
                return;
            }
            StackTraceArguments stackTraceArgs = new StackTraceArguments();
            stackTraceArgs.setThreadId(args.getThreadId());
            try {
                ThreadsResponse threadsResp = dapClientConnector.getRequestManager().threads();
                if (Arrays.stream(threadsResp.getThreads()).noneMatch(t -> t.getId().equals(args.getThreadId()))) {
                    return;
                }
                StackTraceResponse stackTraceResp = dapClientConnector.getRequestManager().
                        stackTrace(stackTraceArgs);
                StackFrame[] stackFrames = stackTraceResp.getStackFrames();
                if (stackFrames.length > 0) {
                    XBreakpoint<BallerinaBreakpointProperties> breakpoint = findBreakPoint(stackFrames[0]);
                    // Get the current suspend context from the session. If the context is null, we need to create a new
                    // context. If the context is not null, we need to add a new execution stack to the current suspend
                    // context.
                    XSuspendContext context = getSession().getSuspendContext();
                    if (context == null) {
                        context = new BallerinaSuspendContext(BallerinaDebugProcess.this);
                    }
                    ((BallerinaSuspendContext) context).addToExecutionStack(args.getThreadId(), stackFrames);
                    XDebugSession session = getSession();
                    if (breakpoint == null) {
                        session.positionReached(context);
                    } else {
                        session.breakpointReached(breakpoint, null, context);
                    }
                }

            } catch (Exception e) {
                LOGGER.warn("Error occurred when fetching stack frames", e);
            }
        });
    }

    // Todo - When to use?
    public void stopRemoteDebugSession() {
        // If we don 't call executeOnPooledThread() here, session will not be stopped correctly since this is
        // called from netty. It seems like this is a blocking action and netty throws an exception.
        ApplicationManager.getApplication().executeOnPooledThread(
                () -> {
                    XDebugSession session = getSession();
                    if (session != null) {
                        session.sessionResumed();
                        session.stop();
                    }
                    getSession().getConsoleView().print("Remote debugging finished.\n",
                            ConsoleViewContentType.SYSTEM_OUTPUT);

                }
        );
    }

    private void initBreakpointHandlersAndSetBreakpoints() {
        if (!breakpointsInitiated.compareAndSet(false, true)) {
            return;
        }
        doSetBreakpoints();
    }

    private void doSetBreakpoints() {
        ReadAction.run(() -> {
            try {
                getSession().initBreakpoints();
            } catch (Exception e) {
                LOGGER.warn("Error occurred when initializing breakpoints.");
            }
        });
    }

    private XBreakpoint<BallerinaBreakpointProperties> findBreakPoint(@NotNull StackFrame stackFrame) {
        String filePath = stackFrame.getSource().getPath().trim();
        int lineNumber = stackFrame.getLine().intValue();

        for (XBreakpoint<BallerinaBreakpointProperties> breakpoint : breakpoints) {
            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                continue;
            }
            VirtualFile fileInBreakpoint = breakpointPosition.getFile();
            int line = breakpointPosition.getLine() + 1;
            if (fileInBreakpoint.getPath().trim().endsWith(filePath) && line == lineNumber) {
                return breakpoint;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public XValueMarkerProvider<?, ?> createValueMarkerProvider() {
        return super.createValueMarkerProvider();
    }

    @Override
    public String getCurrentStateMessage() {
        return dapClientConnector.getState();
    }

    @NotNull
    @Override
    public XDebugTabLayouter createTabLayouter() {
        return super.createTabLayouter();
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return super.getEvaluator();
    }

    private final List<XBreakpoint<BallerinaBreakpointProperties>> breakpoints = ContainerUtil.createConcurrentList();

    private class BallerinaBreakpointHandler extends
            XBreakpointHandler<XLineBreakpoint<BallerinaBreakpointProperties>> {

        BallerinaBreakpointHandler() {
            super(BallerinaBreakPointType.class);
        }

        @Override
        public void registerBreakpoint(@NotNull XLineBreakpoint<BallerinaBreakpointProperties> breakpoint) {
            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                return;
            }
            getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
            if (isConnected && !breakpoints.contains(breakpoint)) {
                breakpoints.add(breakpoint);
                sendBreakpoints(Collections.singletonList(breakpoint), false);
            }
        }

        @Override
        public void unregisterBreakpoint(@NotNull XLineBreakpoint<BallerinaBreakpointProperties> breakpoint,
                                         boolean temporary) {
            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                return;
            }
            if (isConnected && breakpoints.contains(breakpoint)) {
                breakpoints.remove(breakpoint);
                sendBreakpoints(Collections.singletonList(breakpoint), false);
            }
        }

        void sendBreakpoints(List<XBreakpoint<BallerinaBreakpointProperties>> breakpointList, boolean attach) {
            if (!isConnected) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                Map<Source, List<SourceBreakpoint>> sourceBreakpoints = new HashMap<>();
                if (getSession().areBreakpointsMuted()) {
                    return;
                }
                // Transforms IDEA breakpoint DAP breakpoints.
                for (XBreakpoint<BallerinaBreakpointProperties> bp : breakpointList) {
                    if (bp.getType().getId().equals("BallerinaLineBreakpoint") && bp.getSourcePosition() != null) {
                        Source source = new Source();
                        source.setName(bp.getSourcePosition().getFile().getName());
                        source.setPath(bp.getSourcePosition().getFile().getPath());

                        SourceBreakpoint dapBreakpoint = new SourceBreakpoint();
                        dapBreakpoint.setLine((long) bp.getSourcePosition().getLine() + 1);
                        if (sourceBreakpoints.get(source) == null) {
                            sourceBreakpoints.put(source, new ArrayList<>(Collections.singleton(dapBreakpoint)));
                        } else {
                            sourceBreakpoints.get(source).add(dapBreakpoint);
                        }
                    }
                }

                // Sends "setBreakpoints()" requests per source file.
                for (Map.Entry<Source, List<SourceBreakpoint>> entry : sourceBreakpoints.entrySet()) {
                    SetBreakpointsArguments breakpointRequestArgs = new SetBreakpointsArguments();
                    breakpointRequestArgs.setSource(entry.getKey());
                    breakpointRequestArgs.setBreakpoints(entry.getValue().toArray(new SourceBreakpoint[0]));
                    try {
                        dapClientConnector.getRequestManager().setBreakpoints(breakpointRequestArgs);
                    } catch (Exception e) {
                        LOGGER.warn("Breakpoints send request failed.", e);
                    }
                }

                if (attach) {
                    try {
                        // Sends "configuration done" notification to the debug server.
                        dapClientConnector.getRequestManager().configurationDone(new ConfigurationDoneArguments());
                    } catch (Exception e) {
                        LOGGER.warn("Configuration done request failed.", e);
                    }
                    // Sends attach request to the debug server.
                    LOGGER.debug("Sending Attach command.");
                    dapClientConnector.attachToServer();
                    getSession().getConsoleView().print("Compiling...\n", ConsoleViewContentType.SYSTEM_OUTPUT);
                }
            });
        }
    }

    @Override
    public void registerAdditionalActions(@NotNull DefaultActionGroup leftToolbar,
                                          @NotNull DefaultActionGroup topToolbar,
                                          @NotNull DefaultActionGroup settings) {
        super.registerAdditionalActions(leftToolbar, topToolbar, settings);
        topToolbar.remove(ActionManager.getInstance().getAction(XDebuggerActions.RUN_TO_CURSOR));
    }
}
