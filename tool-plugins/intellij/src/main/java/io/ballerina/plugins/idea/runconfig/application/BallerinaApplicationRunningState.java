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

import com.google.common.base.Strings;
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
import io.ballerina.plugins.idea.configuration.BallerinaProjectSettings;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import io.ballerina.plugins.idea.runconfig.BallerinaRunningState;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import io.ballerina.plugins.idea.util.BallerinaExecutor;
import io.ballerina.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_SRC_DIR_NAME;

/**
 * Represents Ballerina application running state.
 */
public class BallerinaApplicationRunningState extends BallerinaRunningState<BallerinaApplicationConfiguration> {

    private int myDebugPort = 5006;
    @Nullable
    public BallerinaHistoryProcessListener myHistoryProcessHandler;

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
            public void startNotified(@NotNull ProcessEvent event) {
                if (myHistoryProcessHandler != null) {
                    myHistoryProcessHandler.apply(processHandler);
                }
            }
        });
        return processHandler;
    }

    @Override
    protected BallerinaExecutor patchExecutor(@NotNull BallerinaExecutor executor) throws ExecutionException {

        Project project = myConfiguration.getProject();
        String projectPath = project.getBasePath();
        if (projectPath == null) {
            return super.patchExecutor(executor);
        }

        String filePath;

        // Find the file in the project. This is needed to find the module. Otherwise if the file is in a sub-module
        // and the SDK for the project is not set, SDK home path will be null.
        PsiFile file = BallerinaPsiImplUtil.findFileInProject(project, myConfiguration.getFilePath());
        if (file == null) {
            return executor;
        }

        VirtualFile fileDir = file.getVirtualFile().getParent();
        // Sets IDEA project as the termination for the recursive search.
        String projectRoot = BallerinaSdkUtils.searchForBallerinaProjectRoot(fileDir.getPath(),
                project.getBasePath());
        // if no ballerina project is found.
        if (projectRoot.isEmpty()) {
            filePath = Paths.get(file.getVirtualFile().getPath()).toString();
        } else {
            String relativeFilePath =
                    Paths.get(file.getVirtualFile().getPath()).toString().replace(projectRoot, "").substring(1);
            //If file is found in a ballerina package, runs the whole package.
            if (relativeFilePath.startsWith(BALLERINA_SRC_DIR_NAME)) {
                filePath = relativeFilePath.replace(BALLERINA_SRC_DIR_NAME + File.separator, "");
                if (filePath.contains(File.separator)) {
                    // package, instead of the single file.
                    filePath = filePath.substring(0, filePath.indexOf(File.separator));
                } else {
                    filePath = relativeFilePath;
                }
            } else {
                filePath = relativeFilePath;
            }
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        if (module == null) {
            throw new ExecutionException("Cannot find module for the file '" + file.getVirtualFile().getPath() + "'");
        }
        if (filePath.isEmpty()) {
            filePath = myConfiguration.getFilePath();
            // Note File.separator will not work here since filepath contains "/" regardless of the OS.
            filePath = filePath.replace(projectPath + "/", "");
        }

        BallerinaExecutor ballerinaExecutor;
        if (!projectRoot.isEmpty()) {
            ballerinaExecutor = executor.withParameters("run");

            // If debugging mode is running, we need to add the debugging flag.
            if (isDebug()) {
                ballerinaExecutor.withParameters("--debug", String.valueOf(myDebugPort));
            }
            ballerinaExecutor.withParameters("--sourceroot").withParameters(projectRoot)
                    .withParameterString(myConfiguration.getBallerinaToolParams());

            if (BallerinaProjectSettings.getStoredSettings(project).isAllowExperimental()) {
                ballerinaExecutor.withParameters("--experimental");
            }

            // Tries to get the ballerina executable path using the SDK.
            String balSdkPath = BallerinaSdkService.getInstance(getConfiguration().getProject()).getSdkHomePath(module);
            // If any sdk is not found and user has chosen to auto detect ballerina home.
            if (Strings.isNullOrEmpty(balSdkPath) && BallerinaProjectSettings.getStoredSettings(project).
                    isAutodetect()) {
                balSdkPath = BallerinaSdkUtils.autoDetectSdk(project);
            }
            ballerinaExecutor.withBallerinaPath(balSdkPath);

            // Todo - remove after fixing ballerina script
            Map<String, String> balHomeEnv = new HashMap<>();
            balHomeEnv.put("BALLERINA_HOME", balSdkPath);
            ballerinaExecutor.withExtraEnvironment(balHomeEnv);
        } else {
            ballerinaExecutor = executor.withParameters("run");

            // If debugging mode is running, we need to add the debugging flag.
            if (isDebug()) {
                ballerinaExecutor.withParameters("--debug", String.valueOf(myDebugPort));
            }

            if (BallerinaProjectSettings.getStoredSettings(project).isAllowExperimental()) {
                ballerinaExecutor.withParameters("--experimental");
            }

            ballerinaExecutor.withParameterString(myConfiguration.getBallerinaToolParams());

            // Tries to get the ballerina executable path using the SDK.
            String balSdkPath = BallerinaSdkService.getInstance(getConfiguration().getProject()).getSdkHomePath(module);
            // If any sdk is not found and user has chosen to auto detect ballerina home.
            if (Strings.isNullOrEmpty(balSdkPath) && BallerinaProjectSettings.getStoredSettings(project)
                    .isAutodetect()) {
                balSdkPath = BallerinaSdkUtils.autoDetectSdk(project);
            }
            ballerinaExecutor.withBallerinaPath(balSdkPath);

            // Todo - remove after fixing ballerina script
            Map<String, String> balHomeEnv = new HashMap<>();
            balHomeEnv.put("BALLERINA_HOME", balSdkPath);
            ballerinaExecutor.withExtraEnvironment(balHomeEnv);
        }

        // Adds ballerina file/package name after flags.
        ballerinaExecutor.withParameters(filePath);

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
