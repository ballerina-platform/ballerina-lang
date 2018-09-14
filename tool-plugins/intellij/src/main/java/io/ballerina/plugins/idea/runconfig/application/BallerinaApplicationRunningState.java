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
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import io.ballerina.plugins.idea.util.BallerinaExecutor;
import io.ballerina.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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
        // RunConfigurationKind kind = getConfiguration().getRunKind();
        Project project = myConfiguration.getProject();
        VirtualFile baseDir = project.getBaseDir();
        String filePath;

        // Find the file in the project. This is needed to find the module. Otherwise if the file is in a sub-module
        // and the SDK for the project is not set, SDK home path will be null.
        PsiFile file = BallerinaPsiImplUtil.findFileInProject(project, myConfiguration.getFilePath());

        assert file != null;
        VirtualFile fileDir = file.getVirtualFile().getParent();
        // Sets source root of the IDEA project as the termination of the recursive search.
        String rootDir = project.getBasePath();
        String sourcerootDir = getSourceRoot(fileDir.getPath(), rootDir);

        if (sourcerootDir.equals("")) {  // if no ballerina project is found.
            sourcerootDir = baseDir.getPath();
            filePath = file.getVirtualFile().getPath().replace(sourcerootDir, "").substring(1);
        } else {
            String relativeFilePath = file.getVirtualFile().getPath().replace(sourcerootDir, "").substring(1);
            if (relativeFilePath.contains(File.separator)) { //If file is found in a ballerina package, runs the whole
                // package, instead of the single file.
                filePath = relativeFilePath.substring(0, relativeFilePath.indexOf(File.separator));
            } else {
                filePath = relativeFilePath;
            }
        }

        Module module;
        module = ModuleUtilCore.findModuleForPsiElement(file);
        if (module == null) {
            throw new ExecutionException("Cannot find module for the file '" + file.getVirtualFile().getPath() + "'");
        }
        if (filePath.isEmpty()) {
            filePath = myConfiguration.getFilePath();
            if (baseDir != null) {
                // Note File.separator will not work here since filepath contains "/" regardless of the OS.
                filePath = filePath.replace(baseDir.getPath() + "/", "");
            }
        }

        BallerinaExecutor ballerinaExecutor;
        if (baseDir != null) {
            ballerinaExecutor = executor.withParameters("run");

            // If debugging mode is running, we need to add the debugging flag.
            if (isDebug()) {
                ballerinaExecutor.withParameters("--debug", String.valueOf(myDebugPort));
            }
            ballerinaExecutor.withParameters("--sourceroot").withParameters(sourcerootDir).withBallerinaPath(
                    BallerinaSdkService.getInstance(getConfiguration().getProject()).getSdkHomePath(module))
                    .withParameterString(myConfiguration.getBallerinaToolParams());
        } else {
            ballerinaExecutor = executor.withParameters("run");

            // If debugging mode is running, we need to add the debugging flag.
            if (isDebug()) {
                ballerinaExecutor.withParameters("--debug", String.valueOf(myDebugPort));
            }
            ballerinaExecutor.withBallerinaPath(
                    BallerinaSdkService.getInstance(getConfiguration().getProject()).getSdkHomePath(module))
                    .withParameterString(myConfiguration.getBallerinaToolParams());
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

    /**
     * Searches for a ballerina project using outward recursion starting from the file directory, until the root
     * directory is found.
     */
    private String getSourceRoot(String currentPath, String root) {

        if (currentPath.equals(root) || currentPath.equals("") || root.equals("")) {
            return "";
        } else {
            File currentDir = new File(currentPath);
            File[] files = currentDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    //skips the .ballerina folder in the user home directory.
                    if (f.isDirectory() && !f.getParentFile().getAbsolutePath().equals(System.getProperty("user.home"))
                            && f.getName().equals(".ballerina")) {
                        return currentDir.getAbsolutePath();
                    }
                }
            }
            return getSourceRoot(currentDir.getParentFile().getAbsolutePath(), root);
        }
    }
}
