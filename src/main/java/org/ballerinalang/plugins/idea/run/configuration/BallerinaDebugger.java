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
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.ballerinalang.plugins.idea.debugger.BallerinaDebugProcess;
import org.ballerinalang.plugins.idea.debugger.BallerinaWebSocketConnector;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationConfiguration;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationRunningState;
import org.ballerinalang.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.ServerSocket;

public class BallerinaDebugger extends GenericProgramRunner {

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

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state,
                                             @NotNull ExecutionEnvironment env) throws ExecutionException {
        if (state instanceof BallerinaApplicationRunningState) {
            FileDocumentManager.getInstance().saveAllDocuments();
            BallerinaHistoryProcessListener historyProcessListener = new BallerinaHistoryProcessListener();

            int port = findFreePort();
            FileDocumentManager.getInstance().saveAllDocuments();

            if (!isRemoteDebuggingEnabled(env)) {
                ((BallerinaApplicationRunningState) state).setHistoryProcessHandler(historyProcessListener);
                ((BallerinaApplicationRunningState) state).setDebugPort(port);
            }

            return XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {

                @NotNull
                @Override
                public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                    if (isRemoteDebuggingEnabled(env)) {
                        // Get the remote host address.
                        String address = getRemoteAddress(env);
                        // Create a new connector. This will be used to communicate with the debugger.
                        BallerinaWebSocketConnector ballerinaDebugSession = new BallerinaWebSocketConnector(address);
                        return new BallerinaDebugProcess(session, ballerinaDebugSession, null);
                    } else {
                        // Get the host address.
                        String address = NetUtils.getLocalHostString() + ":" + port;
                        // Create a new connector. This will be used to communicate with the debugger.
                        BallerinaWebSocketConnector ballerinaDebugSession = new BallerinaWebSocketConnector(address);
                        return new BallerinaDebugProcess(session, ballerinaDebugSession,
                                getExecutionResults(state, env));
                    }
                }
            }).getRunContentDescriptor();
        }
        return null;
    }

    private ExecutionResult getExecutionResults(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
            throws ExecutionException {
        // Start debugger.
        ExecutionResult executionResult = state.execute(env.getExecutor(), new BallerinaDebugger());
        if (executionResult == null) {
            throw new ExecutionException("Cannot run debugger");
        }
        return executionResult;
    }

    private boolean isRemoteDebuggingEnabled(@NotNull ExecutionEnvironment env) {
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = env.getRunnerAndConfigurationSettings();
        if (runnerAndConfigurationSettings == null) {
            return false;
        }
        RunConfiguration configurationSettings = runnerAndConfigurationSettings.getConfiguration();
        if (configurationSettings instanceof BallerinaApplicationConfiguration) {
            BallerinaApplicationConfiguration applicationConfiguration =
                    (BallerinaApplicationConfiguration) configurationSettings;
            if (applicationConfiguration.isRemoteDebuggingEnabled()) {
                return true;
            }
        }
        return false;
    }

    private String getRemoteAddress(@NotNull ExecutionEnvironment env) {
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = env.getRunnerAndConfigurationSettings();
        if (runnerAndConfigurationSettings == null) {
            return "";
        }
        RunConfiguration configurationSettings = runnerAndConfigurationSettings.getConfiguration();
        if (configurationSettings instanceof BallerinaApplicationConfiguration) {
            BallerinaApplicationConfiguration applicationConfiguration =
                    (BallerinaApplicationConfiguration) configurationSettings;
            String remoteDebugHost = applicationConfiguration.getRemoteDebugHost();
            String remoteDebugPort = applicationConfiguration.getRemoteDebugPort();
            return remoteDebugHost + ":" + remoteDebugPort;
        }
        return "";
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
