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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
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
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import org.ballerinalang.plugins.idea.debugger.breakpoint.BallerinaBreakPointType;
import org.ballerinalang.plugins.idea.debugger.breakpoint.BallerinaBreakpointProperties;

import org.ballerinalang.plugins.idea.debugger.dto.BreakPoint;
import org.ballerinalang.plugins.idea.debugger.dto.Message;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.HyperlinkListener;

public class BallerinaDebugProcess extends XDebugProcess {

    //    private WebSocket mySocket;
    private final ExecutionResult myExecutionResult;
    private final ProcessHandler myProcessHandler;
    private final ExecutionConsole myExecutionConsole;
    private final BallerinaDebuggerEditorsProvider myEditorsProvider;
    private final BallerinaBreakpointHandler myBreakPointHandler;
    private final BallerinaWebSocketConnector myConnector;

    private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();
    private final AtomicBoolean connectedListenerAdded = new AtomicBoolean();

    private final int MAX_RETRIES = 10;

    public BallerinaDebugProcess(@NotNull XDebugSession session, @NotNull BallerinaWebSocketConnector connector,
                                 @NotNull ExecutionResult executionResult) {
        super(session);

        myConnector = connector;
        myExecutionResult = executionResult;
        myProcessHandler = executionResult.getProcessHandler();
        myExecutionConsole = executionResult.getExecutionConsole();
        myEditorsProvider = new BallerinaDebuggerEditorsProvider();
        myBreakPointHandler = new BallerinaBreakpointHandler();
        // Todo - add session
        //        myDebuggerSession = XsltDebuggerSession.getInstance(myProcessHandler);

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
        System.out.println("Init called");
        ApplicationManager.getApplication().invokeLater(() -> {
            for (int retries = 0; retries < MAX_RETRIES; retries++) {
                System.out.println("Retry: " + retries);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
                System.out.println("IsConnected: " + myConnector.isConnected());
                if (!myConnector.isConnected()) {
                    myConnector.createConnection();
                    System.out.println("reconnect called");
                    if (myConnector.isConnected()) {
                        addDebugHitListener();
                        System.out.println("Is connected now.");
                        System.out.println("sending breakpoints");
                        initBreakpointHandlersAndSetBreakpoints(true);
                        myBreakPointHandler.sendBreakpoints();
                        System.out.println("sending start command");
                        myConnector.sendCommand(Command.START);
                        break;
                    }
                } else {
                    System.out.println("reconnect success");
                    break;
                }
            }
        });
    }

    private void addDebugHitListener() {
        myConnector.addListener(
                new BallerinaWebSocketAdaptor() {

                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                            throws Exception {
                        initBreakpointHandlersAndSetBreakpoints(true);
                        myConnector.sendCommand(Command.START);
                    }


                    @Override
                    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws
                            Exception {

                        String json = frame.getPayloadText();

                        ObjectMapper mapper = new ObjectMapper();
                        Message message = null;
                        try {
                            message = mapper.readValue(json, Message.class);
                        } catch (IOException e) {

                        }

                        if (message == null) {
                            System.out.println(json);
                            return;
                        }

                        if (Response.DEBUG_HIT.name().equals(message.getCode())) {
                            XBreakpoint<BallerinaBreakpointProperties> breakpoint
                                    = findBreakPoint(message.getLocation());
                            BallerinaSuspendContext context =
                                    new BallerinaSuspendContext(BallerinaDebugProcess.this, message);
                            XDebugSession session = getSession();
                            if (breakpoint == null) {
                                session.positionReached(context);
                            } else {
                                session.breakpointReached(breakpoint, null, context);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void startPausing() {
        super.startPausing();
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.STEP_OVER);
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {

    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.STEP_IN);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.STEP_OUT);
    }

    @Nullable
    @Override
    public XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
        return super.getSmartStepIntoHandler();
    }

    @Override
    public void stop() {
        myConnector.sendCommand(Command.STOP);
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        myConnector.sendCommand(Command.RESUME);
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        // Todo - Implement
    }

    @Override
    public boolean checkCanPerformCommands() {
        return super.checkCanPerformCommands();
    }

    @Override
    public boolean checkCanInitBreakpoints() {

        if (myConnector.isConnected()) {
            // breakpointsInitiated could be set in another thread and at this point work (init breakpoints) could be
            // not yet performed
            return initBreakpointHandlersAndSetBreakpoints(true);
        }

        if (connectedListenerAdded.compareAndSet(false, true)) {
            addDebugHitListener();
        }
        return false;
    }

    private boolean initBreakpointHandlersAndSetBreakpoints(boolean setBreakpoints) {
        if (!breakpointsInitiated.compareAndSet(false, true)) {
            return false;
        }
        if (setBreakpoints) {
            doSetBreakpoints();
        }
        return true;
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

            // Todo - get relative package path
            if (file.getName().equals(fileName) && line == lineNumber) {
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
    public void registerAdditionalActions(@NotNull DefaultActionGroup leftToolbar,
                                          @NotNull DefaultActionGroup topToolbar,
                                          @NotNull DefaultActionGroup settings) {
        super.registerAdditionalActions(leftToolbar, topToolbar, settings);
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

    @Override
    public boolean isValuesCustomSorted() {
        return super.isValuesCustomSorted();
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return super.getEvaluator();
    }

    @Override
    public boolean isLibraryFrameFilterSupported() {
        return super.isLibraryFrameFilterSupported();
    }

    //    private static final Key<Integer> ID = Key.create("DLV_BP_ID");
    private final List<XBreakpoint<BallerinaBreakpointProperties>> breakpoints = ContainerUtil.createConcurrentList();

    private class BallerinaBreakpointHandler extends
            XBreakpointHandler<XLineBreakpoint<BallerinaBreakpointProperties>> {

        public BallerinaBreakpointHandler() {
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

            //            send(new BallerinaRequest.CreateBreakpoint(file.getPath(), line + 1))
            //                    .done(b -> {
            //                        breakpoint.putUserData(ID, b.id);
            //                        breakpoints.put(b.id, breakpoint);
            //                        getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger
            //                                        .Db_verified_breakpoint,
            //                                null);
            //                    })
            //                    .rejected(t -> {
            //                        String message = t == null ? null : t.getMessage();
            //                        getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger
            // .Db_invalid_breakpoint,
            //                                message);
            //                    });

        }

        @Override
        public void unregisterBreakpoint(@NotNull XLineBreakpoint<BallerinaBreakpointProperties> breakpoint,
                                         boolean temporary) {

            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
            if (breakpointPosition == null) {
                return;
            }

            //            Integer id = breakpoint.getUserData(ID);
            //            if (id == null) {
            //                return; // obsolete
            //            }
            //            breakpoint.putUserData(ID, null);
            //            breakpoints.remove(id);
            //                send(new BallerinaRequest.ClearBreakpoint(id));
            breakpoints.remove(breakpoint);

            sendBreakpoints();
        }

        public void sendBreakpoints() {
            StringBuilder stringBuilder = new StringBuilder("{\"command\":\"").append(Command.SET_POINTS)
                    .append("\", \"points\": [");
            if (!getSession().areBreakpointsMuted()) {
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
                        name = name.replaceAll("\\.", "/") + "/";
                    }
                    name += file.getName();

                    stringBuilder.append("{\"fileName\":\"").append(name).append("\"");
                    stringBuilder.append(", \"lineNumber\":").append(line + 1).append("}");
                    if (i < size - 1) {
                        stringBuilder.append(",");
                    }
                }
            }
            stringBuilder.append("]}");
            myConnector.send(stringBuilder.toString());
        }
    }
}

//public class BallerinaDebugProcess extends DebugProcessImpl<VmConnection<?>> implements Disposable {
//
//    private final static Logger LOG = Logger.getInstance(BallerinaDebugProcess.class);
//    private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();
//    private final AtomicBoolean connectedListenerAdded = new AtomicBoolean();
//    private static final Consumer<Throwable> THROWABLE_CONSUMER = LOG::info;
//
//    @NotNull
//    private final Consumer<DebuggerState> myStateConsumer = new Consumer<DebuggerState>() {
//        @Override
//        public void consume(@NotNull DebuggerState o) {
//            if (o.exited) {
//                stop();
//                return;
//            }
//
//            XBreakpoint<BallerinaBreakpointProperties> find = findBreak(o.breakPoint);
//            send(new BallerinaRequest.StacktraceGoroutine()).done(locations -> {
//                BallerinaSuspendContext context = new BallerinaSuspendContext(BallerinaDebugProcess.this,
//                        o.currentThread.id, locations, getProcessor());
//                XDebugSession session = getSession();
//                if (find == null) {
//                    session.positionReached(context);
//                } else {
//                    session.breakpointReached(find, null, context);
//                }
//            });
//        }
//
//        @Nullable
//        private XBreakpoint<BallerinaBreakpointProperties> findBreak(@Nullable Breakpoint point) {
//            return point == null ? null : breakpoints.get(point.id);
//        }
//    };
//
//    @NotNull
//    private <T> Promise<T> send(@NotNull BallerinaRequest<T> request) {
//        return send(request, getProcessor());
//    }
//
//    @NotNull
//    static <T> Promise<T> send(@NotNull BallerinaRequest<T> request, @NotNull BallerinaCommandProcessor processor) {
//        return processor.send(request).rejected(THROWABLE_CONSUMER);
//    }
//
//    @NotNull
//    private BallerinaCommandProcessor getProcessor() {
//        return assertNotNull(tryCast(getVm(), BallerinaVM.class)).getCommandProcessor();
//    }
//
//    public BallerinaDebugProcess(@NotNull XDebugSession session, @NotNull VmConnection<?> connection, @Nullable
//            ExecutionResult er) {
//        super(session, connection, new MyEditorsProvider(), null, er);
//    }
//
//    @NotNull
//    @Override
//    protected XBreakpointHandler<?>[] createBreakpointHandlers() {
//        return new XBreakpointHandler[]{new BallerinaBreakpointHandler()};
//    }
//
//    @NotNull
//    @Override
//    public ExecutionConsole createConsole() {
//        ExecutionResult executionResult = getExecutionResult();
//        return executionResult == null ? super.createConsole() : executionResult.getExecutionConsole();
//    }
//
//    @Override
//    protected boolean isVmStepOutCorrect() {
//        return false;
//    }
//
//    @Override
//    public void dispose() {
//        // todo
//    }
//
//    @Override
//    public boolean checkCanInitBreakpoints() {
//        if (getConnection().getState().getStatus() == ConnectionStatus.CONNECTED) {
//            // breakpointsInitiated could be set in another thread and at this point work (init breakpoints) could be
//            // not yet performed
//            return initBreakpointHandlersAndSetBreakpoints(false);
//        }
//
//        if (connectedListenerAdded.compareAndSet(false, true)) {
//            getConnection().addListener(status -> {
//                if (status == ConnectionStatus.CONNECTED) {
//                    initBreakpointHandlersAndSetBreakpoints(true);
//                }
//            });
//        }
//        return false;
//    }
//
//    private boolean initBreakpointHandlersAndSetBreakpoints(boolean setBreakpoints) {
//        if (!breakpointsInitiated.compareAndSet(false, true)) return false;
//
//        Vm vm = getVm();
//        assert vm != null : "Vm should be initialized";
//
//        if (setBreakpoints) {
//            doSetBreakpoints();
//            resume(vm);
//        }
//
//        return true;
//    }
//
//    private void doSetBreakpoints() {
//        AccessToken token = ReadAction.start();
//        try {
//            getSession().initBreakpoints();
//        } finally {
//            token.finish();
//        }
//    }
//
//    private void command(@NotNull @MagicConstant(stringValues = {NEXT, CONTINUE, HALT, SWITCH_THREAD, STEP})
//                                 String name) {
//        send(new BallerinaRequest.Command(name)).done(myStateConsumer);
//    }
//
//    @Nullable
//    @Override
//    protected Promise<?> continueVm(@NotNull Vm vm, @NotNull StepAction stepAction) {
//        switch (stepAction) {
//            case CONTINUE:
//                command(CONTINUE);
//                break;
//            case IN:
//                command(STEP);
//                break;
//            case OVER:
//                command(NEXT);
//                break;
//            case OUT:
//                // todo
//                break;
//        }
//        return null;
//    }
//
//    @NotNull
//    @Override
//    public List<Location> getLocationsForBreakpoint(@NotNull XLineBreakpoint<?> breakpoint) {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
//        // todo
//    }
//
//    @Override
//    public void stop() {
//        if (getVm() != null) {
//            send(new BallerinaRequest.Detach(true));
//        }
//        getSession().stop();
//    }
//
//    private static class MyEditorsProvider extends XDebuggerEditorsProviderBase {
//
//        @NotNull
//        @Override
//        public FileType getFileType() {
//            return BallerinaFileType.INSTANCE;
//        }
//
//        @Override
//        protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text,
//                                                       @Nullable PsiElement context, boolean isPhysical) {
//            return PsiFileFactory.getInstance(project).createFileFromText("ballerina-debug.txt",
//                    PlainTextLanguage.INSTANCE, text);
//        }
//    }
//
//    private static final Key<Integer> ID = Key.create("DLV_BP_ID");
//    private final Map<Integer, XBreakpoint<BallerinaBreakpointProperties>> breakpoints =
//            ContainerUtil.newConcurrentMap();
//
//    private class BallerinaBreakpointHandler extends
// XBreakpointHandler<XLineBreakpoint<BallerinaBreakpointProperties>> {
//
//        public BallerinaBreakpointHandler() {
//            super(BallerinaBreakPointType.class);
//        }
//
//        @Override
//        public void registerBreakpoint(@NotNull XLineBreakpoint<BallerinaBreakpointProperties> breakpoint) {
//            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
//            if (breakpointPosition == null) return;
//            VirtualFile file = breakpointPosition.getFile();
//            int line = breakpointPosition.getLine();
//            send(new BallerinaRequest.CreateBreakpoint(file.getPath(), line + 1))
//                    .done(b -> {
//                        breakpoint.putUserData(ID, b.id);
//                        breakpoints.put(b.id, breakpoint);
//                        getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger
// .Db_verified_breakpoint,
//                                null);
//                    })
//                    .rejected(t -> {
//                        String message = t == null ? null : t.getMessage();
//                        getSession().updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_invalid_breakpoint,
//                                message);
//                    });
//        }
//
//        @Override
//        public void unregisterBreakpoint(@NotNull XLineBreakpoint<BallerinaBreakpointProperties> breakpoint,
//                                         boolean temporary) {
//            XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
//            if (breakpointPosition == null) return;
//            Integer id = breakpoint.getUserData(ID);
//            if (id == null) return; // obsolete
//            breakpoint.putUserData(ID, null);
//            breakpoints.remove(id);
//            send(new BallerinaRequest.ClearBreakpoint(id));
//        }
//    }
//}
