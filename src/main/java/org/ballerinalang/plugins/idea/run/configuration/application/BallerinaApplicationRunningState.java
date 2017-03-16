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

package org.ballerinalang.plugins.idea.run.configuration.application;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import org.ballerinalang.plugins.idea.run.configuration.BallerinaRunningState;
import org.ballerinalang.plugins.idea.util.BallerinaExecutor;
import org.ballerinalang.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BallerinaApplicationRunningState extends BallerinaRunningState<BallerinaApplicationConfiguration> {

    private String myOutputFilePath;
    @Nullable
    private BallerinaHistoryProcessListener myHistoryProcessHandler;
    private int myDebugPort = 59090;
    private boolean myCompilationFailed;

    public BallerinaApplicationRunningState(@NotNull ExecutionEnvironment env, @NotNull Module module,
                                            @NotNull BallerinaApplicationConfiguration configuration) {
        super(env, module, configuration);
    }

    @NotNull
    public String getTarget() {
        return myConfiguration.getKind() == BallerinaApplicationConfiguration.Kind.PACKAGE
                ? myConfiguration.getPackage() : myConfiguration.getFilePath();
    }

    @NotNull
    public String getGoBuildParams() {
        return myConfiguration.getBallerinaToolParams();
    }

    public boolean isDebug() {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId());
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        ProcessHandler processHandler = myCompilationFailed ? new BallerinaNopProcessHandler() : super.startProcess();
        processHandler.addProcessListener(new ProcessAdapter() {

            @Override
            public void startNotified(ProcessEvent event) {
                if (myHistoryProcessHandler != null) {
                    myHistoryProcessHandler.apply(processHandler);
                }
            }

            @Override
            public void processTerminated(ProcessEvent event) {
                super.processTerminated(event);
                if (StringUtil.isEmpty(myConfiguration.getOutputFilePath())) {
                    File file = new File(myOutputFilePath);
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        });
        return processHandler;
    }

    @Override
    protected BallerinaExecutor patchExecutor(@NotNull BallerinaExecutor executor) throws ExecutionException {
//        if (isDebug()) {
//            File dlv = dlv();
//            if (dlv.exists() && !dlv.canExecute()) {
//                //noinspection ResultOfMethodCallIgnored
//                dlv.setExecutable(true, false);
//            }
//            return executor.withExePath(dlv.getAbsolutePath())
//                    .withParameters("--listen=localhost:" + myDebugPort, "--headless=true", "exec", myOutputFilePath,
//                            "--");
//        }
        return executor.showGoEnvVariables(false).withExePath(myOutputFilePath);
    }

    @NotNull
    private static File dlv() {
        String dlvPath = System.getProperty("dlv.path");
        if (StringUtil.isNotEmpty(dlvPath)) return new File(dlvPath);
        //        return new File(GoUtil.getPlugin().getPath(),
        //                "lib/dlv/" + (SystemInfo.isMac ? "mac" : SystemInfo.isWindows ? "windows" : "linux") + "/"
        //                        + GoConstants.DELVE_EXECUTABLE_NAME + (SystemInfo.isWindows ? ".exe" : ""));
        return null;
    }

    public void setOutputFilePath(@NotNull String outputFilePath) {
        myOutputFilePath = outputFilePath;
    }

    public void setHistoryProcessHandler(@Nullable BallerinaHistoryProcessListener historyProcessHandler) {
        myHistoryProcessHandler = historyProcessHandler;
    }

    public void setDebugPort(int debugPort) {
        myDebugPort = debugPort;
    }

    public void setCompilationFailed(boolean compilationFailed) {
        myCompilationFailed = compilationFailed;
    }
}
