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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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
import io.ballerina.plugins.idea.debugger.dto.BreakPoint;
import io.ballerina.plugins.idea.debugger.dto.Message;
import io.ballerina.plugins.idea.debugger.protocol.Command;
import io.ballerina.plugins.idea.debugger.protocol.Response;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.HyperlinkListener;

/**
 * Ballerina debug process which handles debugging.
 */
public class BallerinaDebugProcess extends XDebugProcess {

    private static final Logger LOGGER = Logger.getInstance(BallerinaDebugProcess.class);
    private static final Gson GSON = new Gson();

    private final ProcessHandler myProcessHandler;
    private final ExecutionConsole myExecutionConsole;
    private final BallerinaDebuggerEditorsProvider myEditorsProvider;
    private final BallerinaBreakpointHandler myBreakPointHandler;
    private final BallerinaWebSocketConnector myConnector;
    private boolean isDisconnected = false;
    private boolean isRemoteDebugMode = false;

    private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();

    public BallerinaDebugProcess(@NotNull XDebugSession session, @NotNull BallerinaWebSocketConnector connector,
                                 @Nullable ExecutionResult executionResult) {
        super(session);
        myConnector = connector;
        myProcessHandler = executionResult == null ? super.getProcessHandler() : executionResult.getProcessHandler();
        myExecutionConsole = executionResult == null ? super.createConsole() : executionResult.getExecutionConsole();
        myEditorsProvider = new BallerinaDebuggerEditorsProvider();
        myBreakPointHandler = new BallerinaBreakpointHandler();
        if (executionResult == null) {
            isRemoteDebugMode = true;
        }
    }

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return myProcessHandler;
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        return myExecutionConsole;
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[]{myBreakPointHandler};
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return myEditorsProvider;
    }

    @Override
    public void sessionInitialized() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            while (!isDisconnected) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (!myConnector.isConnected()) {
                    LOGGER.debug("Not connected. Retrying...");
                    myConnector.createConnection(this::debugHit);
                    if (myConnector.isConnected()) {
                        if (isRemoteDebugMode) {
                            getSession().getConsoleView().print("Connected to the remote server at " +
                                    myConnector.getDebugServerAddress() + ".\n", ConsoleViewContentType.SYSTEM_OUTPUT);
                        }
                        LOGGER.debug("Connection created.");
                        startDebugSession();
                        break;
                    }
                } else {
                    LOGGER.debug("Connection already created.");
                    startDebugSession();
                    break;
                }
                if (isRemoteDebugMode) {
                    break;
                }
            }
            if (!myConnector.isConnected()) {
                getSession().getConsoleView().print("Connection to debug server at " +
                                myConnector.getDebugServerAddress() + " could not be established.\n",
                        ConsoleViewContentType.ERROR_OUTPUT);
                getSession().stop();
            }
        });
    }

    private void startDebugSession() {
        initBreakpointHandlersAndSetBreakpoints();
        LOGGER.debug("Sending breakpoints.");
        myBreakPointHandler.sendBreakpoints();
        LOGGER.debug("Sending start command.");
        myConnector.sendCommand(Command.START);
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        String workerID = getWorkerID(context);
        if (workerID != null) {
            myConnector.sendCommand(Command.STEP_OVER, workerID);
        }
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        String workerID = getWorkerID(context);
        if (workerID != null) {
            myConnector.sendCommand(Command.STEP_IN, workerID);
        }
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        String workerID = getWorkerID(context);
        if (workerID != null) {
            myConnector.sendCommand(Command.STEP_OUT, workerID);
        }
    }

    @Override
    public void stop() {
        // If we don't call this using the executeOnPooledThread(), the UI will hang until the debug server is stopped.
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            XDebugSession session = getSession();
            if (!isRemoteDebugMode) {
                XSuspendContext suspendContext = session.getSuspendContext();
                if (suspendContext != null) {
                    XExecutionStack activeExecutionStack = suspendContext.getActiveExecutionStack();
                    if (activeExecutionStack instanceof BallerinaSuspendContext.BallerinaExecutionStack) {
                        String workerID = ((BallerinaSuspendContext.BallerinaExecutionStack) activeExecutionStack)
                                .getMyWorkerID();
                        if (workerID != null) {
                            myConnector.sendCommand(Command.STOP, workerID);
                        }
                    }
                } else {
                    session.stop();
                    return;
                }
            } else {
                myConnector.sendCommand(Command.STOP);
                session.stop();
                getSession().getConsoleView().print("Disconnected from the debug server.\n",
                        ConsoleViewContentType.SYSTEM_OUTPUT);
            }

            isDisconnected = true;
            myConnector.close();
        });
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        String threadId = getWorkerID(context);
        if (threadId != null) {
            myConnector.sendCommand(Command.RESUME, threadId);
        }
    }

    @Nullable
    private String getWorkerID(@Nullable XSuspendContext context) {
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
        // We manually initializes the breakpoints after connecting to the debug server.
        return false;
    }

    private void debugHit(String response) {
        LOGGER.debug("Received: " + response);
        Message message;
        try {
            message = GSON.fromJson(response, Message.class);
        } catch (JsonSyntaxException e) {
            LOGGER.debug(e);
            return;
        }

        String code = message.getCode();
        if (Response.DEBUG_HIT.name().equals(code)) {
            ApplicationManager.getApplication().runReadAction(() -> {
                XBreakpoint<BallerinaBreakpointProperties> breakpoint = findBreakPoint(message.getLocation());
                // Get the current suspend context from the session. If the context is null, we need to create a new
                // context. If the context is not null, we need to add a new execution stack to the current suspend
                // context.
                XSuspendContext context = getSession().getSuspendContext();
                if (context == null) {
                    context = new BallerinaSuspendContext(BallerinaDebugProcess.this, message);
                } else {
                    ((BallerinaSuspendContext) context).addToExecutionStack(BallerinaDebugProcess.this, message);
                }
                XDebugSession session = getSession();
                if (breakpoint == null) {
                    session.positionReached(context);
                } else {
                    session.breakpointReached(breakpoint, null, context);
                }
            });
        } else if (Response.EXIT.name().equals(code) || Response.COMPLETE.name().equals(code)) {
            if (isRemoteDebugMode) {
                // If we don't call executeOnPooledThread() here, session will not be stopped correctly since this is
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
        }
    }

    private void initBreakpointHandlersAndSetBreakpoints() {
        if (!breakpointsInitiated.compareAndSet(false, true)) {
            return;
        }
        doSetBreakpoints();
    }

    private void doSetBreakpoints() {
        AccessToken token = ReadAction.start();
        try {
            getSession().initBreakpoints();
        } finally {
            token.finish();
            token.close();
        }
    }

    private XBreakpoint<BallerinaBreakpointProperties> findBreakPoint(@NotNull BreakPoint breakPoint) {
        String fileName = breakPoint.getFileName();
        String packagePath = breakPoint.getPackagePath();
        String relativeFilePathInProject;
        // If the package is ".", full path of the file will be sent as the filename.
        if (".".equals(packagePath)) {
            // Then we need to get the actual filename from the path.
            int index = fileName.lastIndexOf(File.separator);
            if (index <= -1) {
                return null;
            }
            relativeFilePathInProject = fileName.substring(index);
        } else {
            // If the absolute path is not sent, we need to construct the relative file path in the project.
            relativeFilePathInProject = packagePath.replaceAll("\\.", File.separator) + File.separator + fileName;
        }
        int lineNumber = breakPoint.getLineNumber();
        for (XBreakpoint<BallerinaBreakpointProperties> breakpoint : breakpoints) {
            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                continue;
            }
            VirtualFile fileInBreakpoint = breakpointPosition.getFile();
            int line = breakpointPosition.getLine() + 1;
            if (fileInBreakpoint.getPath().endsWith(relativeFilePathInProject) && line == lineNumber) {
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
        return myConnector.getState();
    }

    @Nullable
    @Override
    public HyperlinkListener getCurrentStateHyperlinkListener() {
        return super.getCurrentStateHyperlinkListener();
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
            breakpoints.add(breakpoint);
            sendBreakpoints();
            getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
        }

        @Override
        public void unregisterBreakpoint(@NotNull XLineBreakpoint<BallerinaBreakpointProperties> breakpoint,
                                         boolean temporary) {
            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                return;
            }
            breakpoints.remove(breakpoint);
            sendBreakpoints();
        }

        void sendBreakpoints() {
            StringBuilder stringBuilder = new StringBuilder("{\"command\":\"").append(Command.SET_POINTS)
                    .append("\", \"points\": [");
            if (!getSession().areBreakpointsMuted()) {
                ApplicationManager.getApplication().runReadAction(() -> {
                    int size = breakpoints.size();
                    for (int i = 0; i < size; i++) {
                        XSourcePosition breakpointPosition = breakpoints.get(i).getSourcePosition();
                        if (breakpointPosition == null) {
                            return;
                        }
                        VirtualFile file = breakpointPosition.getFile();
                        int line = breakpointPosition.getLine();
                        Project project = getSession().getProject();

                        // Get package path.
                        String packagePath = BallerinaPsiImplUtil.getPackage(project, file);
                        if (packagePath.isEmpty()) {
                            packagePath = ".";
                        }

                        // Get relative file path in the package.
                        String name = BallerinaPsiImplUtil.getFilePathInPackage(project, file);
                        if (name.isEmpty()) {
                            name = file.getName();
                        }

                        stringBuilder.append("{\"packagePath\":\"");
                        String orgName = BallerinaDebuggerUtils.getOrgName(project);
                        if (orgName != null) {
                            stringBuilder.append(orgName).append("/");
                        }
                        stringBuilder.append(packagePath);
                        if (!".".equals(packagePath)) {
                            stringBuilder.append(":").append(BallerinaDebuggerUtils.getVersion(project));
                        }
                        stringBuilder.append("\", ");
                        stringBuilder.append("\"fileName\":\"").append(name).append("\", ");
                        stringBuilder.append("\"lineNumber\":").append(line + 1).append("}");
                        if (i < size - 1) {
                            stringBuilder.append(",");
                        }
                    }
                });
            }
            stringBuilder.append("]}");
            myConnector.send(stringBuilder.toString());
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
