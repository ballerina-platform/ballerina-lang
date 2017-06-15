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

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.ballerinalang.plugins.idea.debugger.BallerinaDebugProcess;
import org.ballerinalang.plugins.idea.debugger.BallerinaWebSocketAdaptor;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationRunningState;
import org.ballerinalang.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import java.io.IOException;
import java.net.ServerSocket;

public class BallerinaDebugger extends AsyncGenericProgramRunner {

    private static final String ID = "BallerinaDebugger";

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof BallerinaRunConfigurationBase;
    }

    @NotNull
    @Override
    protected Promise<RunProfileStarter> prepare(@NotNull ExecutionEnvironment environment,
                                                 @NotNull RunProfileState state) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        AsyncPromise<RunProfileStarter> buildingPromise = new AsyncPromise<>();
        BallerinaHistoryProcessListener historyProcessListener = new BallerinaHistoryProcessListener();

        //        ((BallerinaApplicationRunningState) state).createCommonExecutor()
        //                .withParameters("run", "main")
        //                .withParameters(((BallerinaApplicationRunningState) state).isDebug() ? new
        // String[]{"--debug", "5005"} :
        //                        ArrayUtil.EMPTY_STRING_ARRAY)
        //                .withProcessListener(historyProcessListener)
        //                .withProcessListener(new ProcessAdapter() {
        //
        //                    @Override
        //                    public void processTerminated(ProcessEvent event) {
        //                        super.processTerminated(event);
        //                        boolean compilationFailed = event.getExitCode() != 0;
        //                        if (((BallerinaApplicationRunningState) state).isDebug()) {
        //                            buildingPromise.setResult(new MyDebugStarter(historyProcessListener,
        // compilationFailed));
        //                        }
        //
        //                    }
        //                }).executeWithProgress(false);

        buildingPromise.setResult(new MyDebugStarter(historyProcessListener));

        return buildingPromise;
    }

    private class MyDebugStarter extends RunProfileStarter {

        private final BallerinaHistoryProcessListener myHistoryProcessListener;

        private MyDebugStarter(@NotNull BallerinaHistoryProcessListener historyProcessListener) {
            myHistoryProcessListener = historyProcessListener;
        }

        @Nullable
        @Override
        public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
                throws ExecutionException {
            if (state instanceof BallerinaApplicationRunningState) {
                int port = findFreePort();
                FileDocumentManager.getInstance().saveAllDocuments();
                ((BallerinaApplicationRunningState) state).setHistoryProcessHandler(myHistoryProcessListener);
                ((BallerinaApplicationRunningState) state).setDebugPort(port);

                // start debugger
                ExecutionResult executionResult = state.execute(env.getExecutor(), BallerinaDebugger.this);
                if (executionResult == null) {
                    throw new ExecutionException("Cannot run debugger");
                }

                return XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
                    @NotNull
                    @Override
                    public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {


                        try {
                            String address = NetUtils.getLoopbackAddress().getHostAddress() + ":" + port;

                            WebSocketFactory webSocketFactory = new WebSocketFactory();

                            WebSocket webSocket = webSocketFactory.createSocket("ws://" + address + "/debug");




                            //                            RemoteVmConnection connection = new
                            // BallerinaRemoteVMConnection();
                            //                            BallerinaDebugProcess process = new BallerinaDebugProcess
                            // (session, connection,
                            //                                    executionResult);
                            //                            connection.open(new InetSocketAddress(NetUtils
                            // .getLoopbackAddress(), port));


                            return new BallerinaDebugProcess(session, webSocket, executionResult);

                        } catch (IOException e) {
                            throw new ExecutionException("Connection failed.");
                        }

                    }
                }).getRunContentDescriptor();
            }
            return null;
        }
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (Exception ignore) {
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start debugging");
    }
}
