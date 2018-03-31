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

package org.ballerinalang.plugins.idea.runconfig;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import org.ballerinalang.plugins.idea.util.BallerinaExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Starts the Ballerina program.
 *
 * @param <T> configuration type
 */
public abstract class BallerinaRunningState<T extends BallerinaRunConfigurationBase<?>> extends CommandLineState {

    @NotNull
    private final Module myModule;

    @NotNull
    public T getConfiguration() {
        return myConfiguration;
    }

    @NotNull
    protected final T myConfiguration;

    public BallerinaRunningState(@NotNull ExecutionEnvironment env, @NotNull Module module, @NotNull T configuration) {
        super(env);
        myModule = module;
        myConfiguration = configuration;
        addConsoleFilters(new BallerinaConsoleFilter(myConfiguration.getProject(), myModule,
                myConfiguration.getWorkingDirectoryUrl()));
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        BallerinaExecutor executor = patchExecutor(createCommonExecutor());
        // We only need to set parameters.
        GeneralCommandLine commandLine = executor.withParameterString(myConfiguration.getParams()).createCommandLine();
        KillableColoredProcessHandler handler = new KillableColoredProcessHandler(commandLine, true);
        ProcessTerminatedListener.attach(handler);
        return handler;
    }

    @NotNull
    public BallerinaExecutor createCommonExecutor() {
        return BallerinaExecutor.in(myModule).withWorkDirectory(myConfiguration.getWorkingDirectory())
                .withExtraEnvironment(myConfiguration.getCustomEnvironment())
                .withPassParentEnvironment(myConfiguration.isPassParentEnvironment());
    }

    protected BallerinaExecutor patchExecutor(@NotNull BallerinaExecutor executor) throws ExecutionException {
        return executor;
    }
}
