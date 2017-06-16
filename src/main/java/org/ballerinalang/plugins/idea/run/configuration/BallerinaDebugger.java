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
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.ballerinalang.plugins.idea.debugger.BallerinaDebugProcess;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationRunningState;
import org.ballerinalang.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;

public class BallerinaDebugger extends GenericProgramRunner {

    private static final String ID = "BallerinaDebugger";
    private WebSocket mySocket;

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof BallerinaRunConfigurationBase;
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state,
                                             @NotNull ExecutionEnvironment env) throws ExecutionException {
        if (state instanceof BallerinaApplicationRunningState) {
            FileDocumentManager.getInstance().saveAllDocuments();
            BallerinaHistoryProcessListener historyProcessListener = new BallerinaHistoryProcessListener();

            int port = findFreePort();
            FileDocumentManager.getInstance().saveAllDocuments();
            ((BallerinaApplicationRunningState) state).setHistoryProcessHandler(historyProcessListener);
            ((BallerinaApplicationRunningState) state).setDebugPort(port);

            // start debugger
            ExecutionResult executionResult = state.execute(env.getExecutor(), new BallerinaDebugger());
            if (executionResult == null) {
                throw new ExecutionException("Cannot run debugger");
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e1) {

            }

            return XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {

                @NotNull
                @Override
                public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                    try {
                        String address = NetUtils.getLoopbackAddress().getHostAddress() + ":" + port;
                        WebSocketFactory webSocketFactory = new WebSocketFactory();
                        webSocketFactory.setConnectionTimeout(10000);
                        mySocket = webSocketFactory.createSocket("ws://" + address + "/debug");

                        ApplicationManager.getApplication().invokeLater(() -> {
                            int MAX_RETRIES = 5;
                            for (int retries = 0; retries < MAX_RETRIES; retries++) {

                                try {
                                    mySocket = mySocket.connect();
                                    break;
                                } catch (Exception e) {

                                }
                            }
                        });
                        return new BallerinaDebugProcess(session, mySocket, executionResult);
                    } catch (IOException e) {
                        throw new ExecutionException("Connection to debugger failed.");
                    }

                }
            }).getRunContentDescriptor();
        }
        return null;
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
