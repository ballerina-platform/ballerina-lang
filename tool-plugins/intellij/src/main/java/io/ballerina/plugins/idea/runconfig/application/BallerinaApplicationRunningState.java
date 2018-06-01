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

package io.ballerina.plugins.idea.runconfig.application;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import io.ballerina.plugins.idea.runconfig.BallerinaRunningState;
import io.ballerina.plugins.idea.runconfig.RunConfigurationKind;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import io.ballerina.plugins.idea.util.BallerinaExecutor;
import io.ballerina.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents Ballerina application running state.
 */
public class BallerinaApplicationRunningState extends BallerinaRunningState<BallerinaApplicationConfiguration> {

    private int myDebugPort = 5006;
    @Nullable
    private BallerinaHistoryProcessListener myHistoryProcessHandler;

    BallerinaApplicationRunningState(@NotNull ExecutionEnvironment env, @NotNull Module module,
                                     @NotNull BallerinaApplicationConfiguration configuration) {
        super(env, module, configuration);
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        ProcessHandler processHandler = super.startProcess();
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void startNotified(ProcessEvent event) {
                if (myHistoryProcessHandler != null) {
                    myHistoryProcessHandler.apply(processHandler);
                }
            }
        });
        return processHandler;
    }

    @Override
    protected BallerinaExecutor patchExecutor(@NotNull BallerinaExecutor executor) throws ExecutionException {
        RunConfigurationKind kind = getConfiguration().getRunKind();
        Project project = myConfiguration.getProject();
        VirtualFile baseDir = project.getBaseDir();
        String filePath = myConfiguration.getPackage();

        // Find the file in the project. This is needed to find the module. Otherwise if the file is in a sub-module
        // and the SDK for the project is not set, SDK home path will be null.
        PsiFile file = BallerinaPsiImplUtil.findFileInProject(project, myConfiguration.getFilePath());
        Module module = null;
        if (file != null) {
            module = ModuleUtilCore.findModuleForPsiElement(file);
        }
        if (module == null) {
            throw new ExecutionException("Cannot find module for the file '" + file.getVirtualFile().getPath() + "'");
        }
        if (filePath.isEmpty()) {
            filePath = myConfiguration.getFilePath();
            if (baseDir != null) {
                // Note File.separator will not work here since filepath contains "/" regardless of the OS.
                filePath = filePath.replace(baseDir.getPath() + "/", "");
            }

            //            if (filePath.contains(File.separator)) {
            //                int index = filePath.indexOf(File.separator);
            //                filePath = filePath.substring(0, index);
            //            }
        }

        BallerinaExecutor ballerinaExecutor;
        if (baseDir != null) {
            ballerinaExecutor = executor
                    .withParameters("run")
                    .withParameters("--sourceroot")
                    .withParameters(baseDir.getPath())
                    .withBallerinaPath(BallerinaSdkService.getInstance(getConfiguration().getProject())
                            .getSdkHomePath(module))
                    .withParameterString(myConfiguration.getBallerinaToolParams()).withParameters(filePath);
        } else {
            ballerinaExecutor = executor
                    .withParameters("run")
                    .withBallerinaPath(BallerinaSdkService.getInstance(getConfiguration().getProject())
                            .getSdkHomePath(module))
                    .withParameterString(myConfiguration.getBallerinaToolParams()).withParameters(filePath);
        }

        //        if (kind == RunConfigurationKind.SERVICE) {
        //            ballerinaExecutor.withParameters("-s");
        //        }

        // If debugging mode is running, we need to add the debugging flag.
        if (isDebug()) {
            ballerinaExecutor.withParameters("--debug", String.valueOf(myDebugPort));
        }
        return ballerinaExecutor;
    }

    public void setDebugPort(int debugPort) {
        myDebugPort = debugPort;
    }

    public void setHistoryProcessHandler(@Nullable BallerinaHistoryProcessListener historyProcessHandler) {
        myHistoryProcessHandler = historyProcessHandler;
    }

    /**
     * Indicates whether the debugging is invoked or not.
     *
     * @return {@code true} if debugging is running, {@code false} otherwise.
     */
    private boolean isDebug() {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId());
    }
}
