/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.debugger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import org.ballerinalang.plugins.idea.debugger.breakpoint.BallerinaBreakPointType;
import org.ballerinalang.plugins.idea.debugger.breakpoint.BallerinaBreakpointProperties;

import org.ballerinalang.plugins.idea.debugger.dto.BreakPoint;
import org.ballerinalang.plugins.idea.debugger.dto.Message;
import org.ballerinalang.plugins.idea.debugger.protocol.Command;
import org.ballerinalang.plugins.idea.debugger.protocol.Response;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import javax.swing.event.HyperlinkListener;

public class BallerinaDebugProcess extends XDebugProcess {

    private static final Logger LOGGER = Logger.getInstance(BallerinaDebugProcess.class);
    private static final Gson GSON = new Gson();

    private final ProcessHandler myProcessHandler;
    private final ExecutionConsole myExecutionConsole;
    private final BallerinaDebuggerEditorsProvider myEditorsProvider;
    private final BallerinaBreakpointHandler myBreakPointHandler;
    private final BallerinaWebSocketConnector myConnector;
    private boolean isDisconnected = false;

    private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();

    public BallerinaDebugProcess(@NotNull XDebugSession session, @NotNull BallerinaWebSocketConnector connector,
                                 @NotNull ExecutionResult executionResult) {
        super(session);
        myConnector = connector;
        myProcessHandler = executionResult.getProcessHandler();
        myExecutionConsole = executionResult.getExecutionConsole();
        myEditorsProvider = new BallerinaDebuggerEditorsProvider();
        myBreakPointHandler = new BallerinaBreakpointHandler();
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
                        LOGGER.debug("Connection created.");
                        startDebugSession();
                        break;
                    }
                } else {
                    LOGGER.debug("Connection already created.");
                    startDebugSession();
                    break;
                }
            }
            if (!myConnector.isConnected()) {
                getSession().getConsoleView().print("Connection to debug server could not be established.",
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
        myConnector.sendCommand(Command.STEP_OVER);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.STEP_IN);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.STEP_OUT);
    }

    @Override
    public void stop() {
        isDisconnected = true;
        myConnector.sendCommand(Command.STOP);
        myConnector.close();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.RESUME);
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
                BallerinaSuspendContext context = new BallerinaSuspendContext(BallerinaDebugProcess.this, message);
                XDebugSession session = getSession();
                if (breakpoint == null) {
                    session.positionReached(context);
                } else {
                    session.breakpointReached(breakpoint, null, context);
                }
            });
        } else if (Response.EXIT.name().equals(code) || Response.COMPLETE.name().equals(code)) {
            getSession().stop();
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
        int lineNumber = breakPoint.getLineNumber();
        for (XBreakpoint<BallerinaBreakpointProperties> breakpoint : breakpoints) {
            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                continue;
            }
            VirtualFile file = breakpointPosition.getFile();
            int line = breakpointPosition.getLine() + 1;

            Project project = getSession().getProject();
            String filePath = file.getName();
            String packageName = BallerinaUtil.suggestPackageNameForFile(project, file);
            if (!packageName.isEmpty()) {
                filePath = packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
                filePath += (Matcher.quoteReplacement(File.separator) + file.getName());
            }

            if (filePath.equals(fileName) && line == lineNumber) {
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

                        String name = "";
                        // Only get relative path if a package declaration is present in the file.
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(psiFile,
                                PackageDeclarationNode.class);
                        if (packageDeclarationNode != null) {
                            name = BallerinaUtil.suggestPackageNameForFile(project, file);
                            // We don't need to use '\' instead of '/' here since the debug server will convert it to
                            // proper separator character.
                            name = name.replaceAll("\\.", "/") + "/";
                        }
                        name += file.getName();

                        stringBuilder.append("{\"fileName\":\"").append(name).append("\"");
                        stringBuilder.append(", \"lineNumber\":").append(line + 1).append("}");
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
}
