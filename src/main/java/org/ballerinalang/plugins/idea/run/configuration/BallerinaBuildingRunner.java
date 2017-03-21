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
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationConfiguration;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationRunningState;
import org.ballerinalang.plugins.idea.sdk.BallerinaEnvironmentUtil;
import org.ballerinalang.plugins.idea.util.BallerinaHistoryProcessListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import java.io.File;
import java.io.IOException;

public class BallerinaBuildingRunner extends AsyncGenericProgramRunner {

    private static final String ID = "BallerinaBuildingRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (profile instanceof BallerinaApplicationConfiguration) {
            return DefaultRunExecutor.EXECUTOR_ID.equals(executorId);
        }
        return false;
    }

    @NotNull
    @Override
    protected Promise<RunProfileStarter> prepare(@NotNull ExecutionEnvironment environment,
                                                 @NotNull RunProfileState state) throws ExecutionException {
        RunnerAndConfigurationSettings configurationSettings = environment.getRunnerAndConfigurationSettings();
        assert configurationSettings != null;
        RunConfiguration configuration = configurationSettings.getConfiguration();
        RunConfigurationKind kind = ((BallerinaApplicationConfiguration) configuration).getRunKind();

        File outputFile = getOutputFile(environment, (BallerinaApplicationRunningState) state,
                kind.name().toLowerCase());
        // Todo - Remove this deletion
        outputFile.delete();
        FileDocumentManager.getInstance().saveAllDocuments();

        AsyncPromise<RunProfileStarter> buildingPromise = new AsyncPromise<>();
        BallerinaHistoryProcessListener historyProcessListener = new BallerinaHistoryProcessListener();
        ((BallerinaApplicationRunningState) state).createCommonExecutor()
                .withParameters("build")
                .withParameters(kind.name().toLowerCase())
                .withParameterString(((BallerinaApplicationRunningState) state).getBallerinaBuildParams())
                //Todo - Add debug flags
                .withParameters(((BallerinaApplicationRunningState) state).getTarget())
                .withParameters("-o", outputFile.getName())
                .disablePty()
                .withPresentableName("ballerina build")
                .withProcessListener(historyProcessListener)
                .withProcessListener(new ProcessAdapter() {

                    @Override
                    public void processTerminated(ProcessEvent event) {
                        super.processTerminated(event);
                        boolean compilationFailed = event.getExitCode() != 0;
                        //Todo - Check for debug
                        buildingPromise.setResult(new MyRunStarter(outputFile.getAbsolutePath(),
                                historyProcessListener, compilationFailed, kind));

                    }
                })
                .executeWithProgress(false);
        return buildingPromise;
    }

    @NotNull
    private static File getOutputFile(@NotNull ExecutionEnvironment environment,
                                      @NotNull BallerinaApplicationRunningState state, String type)
            throws ExecutionException {
        File outputFile;
        String outputDirectoryPath = state.getConfiguration().getOutputFilePath();
        RunnerAndConfigurationSettings settings = environment.getRunnerAndConfigurationSettings();
        String configurationName = settings != null ? settings.getName() : "application";
        if (StringUtil.isEmpty(outputDirectoryPath)) {
            try {
                String suffix = null;
                if ("main".equals(type)) {
                    suffix = ".bmz";
                } else if ("service".equals(type)) {
                    suffix = ".bsz";
                }
                outputFile = FileUtil.createTempFile(configurationName, suffix, true);
            } catch (IOException e) {
                throw new ExecutionException("Cannot create temporary output file", e);
            }
        } else {
            File outputDirectory = new File(outputDirectoryPath);
            if (outputDirectory.isDirectory() || !outputDirectory.exists() && outputDirectory.mkdirs()) {
                outputFile = new File(outputDirectoryPath,
                        BallerinaEnvironmentUtil.getFullBinaryFileName(configurationName, type));
                try {
                    if (!outputFile.exists() && !outputFile.createNewFile()) {
                        throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
                }
            } else {
                throw new ExecutionException("Cannot create output file in " + outputDirectory.getAbsolutePath());
            }
        }
        if (!prepareFile(outputFile)) {
            throw new ExecutionException("Cannot make temporary file executable " + outputFile.getAbsolutePath());
        }
        return outputFile;
    }

    private static boolean prepareFile(@NotNull File file) {
        try {
            FileUtil.writeToFile(file, new byte[]{0x7F, 'E', 'L', 'F'});
        } catch (IOException e) {
            return false;
        }
        return file.setExecutable(true);
    }

    private class MyRunStarter extends RunProfileStarter {

        private final String myOutputFilePath;
        private final BallerinaHistoryProcessListener myHistoryProcessListener;
        private final boolean myCompilationFailed;
        private final RunConfigurationKind myRunKind;

        private MyRunStarter(@NotNull String outputFilePath,
                             @NotNull BallerinaHistoryProcessListener historyProcessListener,
                             boolean compilationFailed, RunConfigurationKind runKind) {
            myOutputFilePath = outputFilePath;
            myHistoryProcessListener = historyProcessListener;
            myCompilationFailed = compilationFailed;
            myRunKind = runKind;
        }

        @Nullable
        @Override
        public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
                throws ExecutionException {
            if (state instanceof BallerinaApplicationRunningState) {
                FileDocumentManager.getInstance().saveAllDocuments();
                ((BallerinaApplicationRunningState) state).setHistoryProcessHandler(myHistoryProcessListener);
                ((BallerinaApplicationRunningState) state).setOutputFilePath(myOutputFilePath);
                ((BallerinaApplicationRunningState) state).setCompilationFailed(myCompilationFailed);
                ((BallerinaApplicationRunningState) state).setKind(myRunKind);
                ExecutionResult executionResult = state.execute(env.getExecutor(), BallerinaBuildingRunner.this);
                return executionResult != null ?
                        new RunContentBuilder(executionResult, env).showRunContent(env.getContentToReuse()) : null;
            }
            return null;
        }
    }
}
