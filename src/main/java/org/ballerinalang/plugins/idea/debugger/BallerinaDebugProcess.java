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

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
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
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import org.ballerinalang.plugins.idea.debugger.breakpoint.BallerinaBreakPointType;
import org.ballerinalang.plugins.idea.debugger.breakpoint.BallerinaBreakpointProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.HyperlinkListener;

public class BallerinaDebugProcess extends XDebugProcess {

    private WebSocket mySocket;
    private ExecutionResult myExecutionResult;
    private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();
    private final AtomicBoolean connectedListenerAdded = new AtomicBoolean();

    public BallerinaDebugProcess(@NotNull XDebugSession session, @NotNull WebSocket socket,
                                 @NotNull ExecutionResult executionResult) {
        super(session);
        this.mySocket = socket;
        this.myExecutionResult = executionResult;
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[]{new MyBreakpointHandler()};
    }

    @Override
    public void sessionInitialized() {
        // To debug the debugger, add a breakpoint here. When it is hit, connect to the ballerina runtime using a
        // remote connection. then continue from here.
        BallerinaWebSocketAdaptor webSocketAdaptor = new BallerinaWebSocketAdaptor();
        try {
            mySocket = mySocket.addListener(webSocketAdaptor);
            mySocket = mySocket.connect();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPausing() {
        super.startPausing();
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        if (mySocket.isOpen()) {
            mySocket.sendText("{\"command\":\"STEP_OVER\"}");
        }
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        super.startForceStepInto(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        if (mySocket.isOpen()) {
            mySocket.sendText("{\"command\":\"STEP_IN\"}");
        }
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        if (mySocket.isOpen()) {
            mySocket.sendText("{\"command\":\"STEP_OUT\"}");
        }
    }

    @Nullable
    @Override
    public XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
        return super.getSmartStepIntoHandler();
    }

    @Override
    public void stop() {
        super.getSession().stop();
        if (mySocket.isOpen()) {
            mySocket.sendText("{\"command\":\"STOP\"}");
        }
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        if (mySocket.isOpen()) {
            mySocket.sendText("{\"command\":\"RESUME\"}");
        }
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        super.runToPosition(position, context);
    }

    @Override
    public boolean checkCanPerformCommands() {
        return super.checkCanPerformCommands();
    }

    @Override
    public boolean checkCanInitBreakpoints() {
        if (mySocket.isOpen()) {
            // breakpointsInitiated could be set in another thread and at this point work (init breakpoints) could be
            // not yet performed
            return initBreakpointHandlersAndSetBreakpoints(false);
        }

        if (connectedListenerAdded.compareAndSet(false, true)) {
            mySocket = mySocket.addListener(
                    new BallerinaWebSocketAdaptor() {

                        @Override
                        public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                                throws Exception {
                            initBreakpointHandlersAndSetBreakpoints(true);
                            mySocket.sendText("{\"command\":\"START\"}");
                        }
                    }
            );
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

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return super.doGetProcessHandler();
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        return myExecutionResult.getExecutionConsole();
    }

    @Nullable
    @Override
    public XValueMarkerProvider<?, ?> createValueMarkerProvider() {
        return super.createValueMarkerProvider();
    }

    @Override
    public void registerAdditionalActions(@NotNull DefaultActionGroup leftToolbar, @NotNull DefaultActionGroup
            topToolbar, @NotNull DefaultActionGroup settings) {
        super.registerAdditionalActions(leftToolbar, topToolbar, settings);
    }

    @Override
    public String getCurrentStateMessage() {
        return super.getCurrentStateMessage();
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

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new BallerinaDebuggerEditorsProvider();
    }

    //    private static final Key<Integer> ID = Key.create("DLV_BP_ID");
    private final List<XBreakpoint<BallerinaBreakpointProperties>> breakpoints = ContainerUtil.createConcurrentList();

    private class MyBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<BallerinaBreakpointProperties>> {

        public MyBreakpointHandler() {
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

        private void sendBreakpoints() {
            StringBuilder stringBuilder = new StringBuilder("{\"command\":\"SET_POINTS\", \"points\": [");
            int size = breakpoints.size();
            for (int i = 0; i < size; i++) {
                XSourcePosition breakpointPosition = breakpoints.get(i).getSourcePosition();
                if (breakpointPosition == null) {
                    return;
                }
                VirtualFile file = breakpointPosition.getFile();
                int line = breakpointPosition.getLine();
                stringBuilder.append("{\"fileName\":\"").append(file.getPath()).append("\"");
                stringBuilder.append(", \"lineNumber\":").append(line + 1).append("}");
                if (i < size - 1) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append("]}");
            mySocket.sendText(stringBuilder.toString());
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
//        return new XBreakpointHandler[]{new MyBreakpointHandler()};
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
//    private class MyBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<BallerinaBreakpointProperties>> {
//
//        public MyBreakpointHandler() {
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
