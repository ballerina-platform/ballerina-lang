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

package org.ballerinalang.plugins.idea.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.ExecutionModes;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.Consumer;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.runconfig.BallerinaConsoleFilter;
import org.ballerinalang.plugins.idea.runconfig.BallerinaRunUtil;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BallerinaExecutor {

    private static final Logger LOGGER = Logger.getInstance(BallerinaExecutor.class);
    @NotNull
    private final Map<String, String> myExtraEnvironment = ContainerUtil.newHashMap();
    @NotNull
    private final ParametersList myParameterList = new ParametersList();
    @NotNull
    private final ProcessOutput myProcessOutput = new ProcessOutput();
    @NotNull
    private final Project myProject;
    @Nullable
    private final Module myModule;
    @Nullable
    private String myBallerinaPath;
    @Nullable
    private String myEnvPath;
    @Nullable
    private String myWorkDirectory;
    private boolean myShowOutputOnError;
    private boolean myShowNotificationsOnError;
    private boolean myShowNotificationsOnSuccess;
    private boolean myShowBallerinaEnvVariables = true;
    private GeneralCommandLine.ParentEnvironmentType myParentEnvironmentType =
            GeneralCommandLine.ParentEnvironmentType.CONSOLE;
    private boolean myPtyDisabled;
    @Nullable
    private String myExePath;
    @Nullable
    private String myPresentableName;
    private OSProcessHandler myProcessHandler;
    private final Collection<ProcessListener> myProcessListeners = ContainerUtil.newArrayList();

    private BallerinaExecutor(@NotNull Project project, @Nullable Module module) {
        myProject = project;
        myModule = module;
    }

    public static BallerinaExecutor in(@NotNull Project project, @Nullable Module module) {
        return module != null ? in(module) : in(project);
    }

    @NotNull
    private static BallerinaExecutor in(@NotNull Project project) {
        return new BallerinaExecutor(project, null);
    }

    @NotNull
    public static BallerinaExecutor in(@NotNull Module module) {
        Project project = module.getProject();
        return new BallerinaExecutor(project, module);
    }

    @NotNull
    public BallerinaExecutor withPresentableName(@Nullable String presentableName) {
        myPresentableName = presentableName;
        return this;
    }

    @NotNull
    public BallerinaExecutor withExePath(@Nullable String exePath) {
        myExePath = exePath;
        return this;
    }

    @NotNull
    public BallerinaExecutor withWorkDirectory(@Nullable String workDirectory) {
        myWorkDirectory = workDirectory;
        return this;
    }


    @NotNull
    public BallerinaExecutor withBallerinaPath(@Nullable String ballerinaPath) {
        myBallerinaPath = ballerinaPath;
        return this;
    }

    @NotNull
    public BallerinaExecutor withEnvPath(@Nullable String envPath) {
        myEnvPath = envPath;
        return this;
    }

    public BallerinaExecutor withProcessListener(@NotNull ProcessListener listener) {
        myProcessListeners.add(listener);
        return this;
    }

    @NotNull
    public BallerinaExecutor withExtraEnvironment(@NotNull Map<String, String> environment) {
        myExtraEnvironment.putAll(environment);
        return this;
    }

    @NotNull
    public BallerinaExecutor withPassParentEnvironment(boolean passParentEnvironment) {
        myParentEnvironmentType = passParentEnvironment ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
                : GeneralCommandLine.ParentEnvironmentType.NONE;
        return this;
    }

    @NotNull
    public BallerinaExecutor withParameterString(@NotNull String parameterString) {
        myParameterList.addParametersString(parameterString);
        return this;
    }

    @NotNull
    public BallerinaExecutor withParameters(@NotNull String... parameters) {
        myParameterList.addAll(parameters);
        return this;
    }

    public BallerinaExecutor showBallerinaEnvVariables(boolean show) {
        myShowBallerinaEnvVariables = show;
        return this;
    }

    @NotNull
    public BallerinaExecutor showOutputOnError() {
        myShowOutputOnError = true;
        return this;
    }

    @NotNull
    public BallerinaExecutor disablePty() {
        myPtyDisabled = true;
        return this;
    }

    @NotNull
    public BallerinaExecutor showNotifications(boolean onError, boolean onSuccess) {
        myShowNotificationsOnError = onError;
        myShowNotificationsOnSuccess = onSuccess;
        return this;
    }

    public boolean execute() {
        Logger.getInstance(getClass()).assertTrue(!ApplicationManager.getApplication().isDispatchThread(),
                "It's bad idea to run external tool on EDT");
        Logger.getInstance(getClass()).assertTrue(myProcessHandler == null,
                "Process has already run with this executor instance");
        Ref<Boolean> result = Ref.create(false);
        GeneralCommandLine commandLine = null;
        try {
            commandLine = createCommandLine();
            GeneralCommandLine finalCommandLine = commandLine;
            myProcessHandler = new KillableColoredProcessHandler(finalCommandLine, true) {
                @Override
                public void startNotify() {
                    if (myShowBallerinaEnvVariables) {
                        BallerinaRunUtil.printBallerinaEnvVariables(finalCommandLine, this);
                    }
                    super.startNotify();
                }
            };
            BallerinaHistoryProcessListener historyProcessListener = new BallerinaHistoryProcessListener();
            myProcessHandler.addProcessListener(historyProcessListener);
            for (ProcessListener listener : myProcessListeners) {
                myProcessHandler.addProcessListener(listener);
            }

            CapturingProcessAdapter processAdapter = new CapturingProcessAdapter(myProcessOutput) {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    super.processTerminated(event);
                    boolean success = event.getExitCode() == 0 && myProcessOutput.getStderr().isEmpty();
                    boolean nothingToShow = myProcessOutput.getStdout().isEmpty()
                            && myProcessOutput.getStderr().isEmpty();
                    boolean cancelledByUser = (event.getExitCode() == -1 || event.getExitCode() == 2) && nothingToShow;
                    result.set(success);
                    if (success) {
                        if (myShowNotificationsOnSuccess) {
                            showNotification("Finished successfully", NotificationType.INFORMATION);
                        }
                    } else if (cancelledByUser) {
                        if (myShowNotificationsOnError) {
                            showNotification("Interrupted", NotificationType.WARNING);
                        }
                    } else if (myShowOutputOnError) {
                        ApplicationManager.getApplication().invokeLater(() -> showOutput(myProcessHandler,
                                historyProcessListener));
                    }
                }
            };

            myProcessHandler.addProcessListener(processAdapter);
            myProcessHandler.startNotify();
            ExecutionModes.SameThreadMode sameThreadMode = new ExecutionModes.SameThreadMode(getPresentableName());
            ExecutionHelper.executeExternalProcess(myProject, myProcessHandler, sameThreadMode, commandLine);

            LOGGER.debug("Finished `" + getPresentableName() + "` with result: " + result.get());
            return result.get();
        } catch (ExecutionException e) {
            if (myShowOutputOnError) {
                ExecutionHelper.showErrors(myProject, Collections.singletonList(e), getPresentableName(), null);
            }
            if (myShowNotificationsOnError) {
                showNotification(StringUtil.notNullize(e.getMessage(), "Unknown error, see logs for details"),
                        NotificationType.ERROR);
            }
            String commandLineInfo = commandLine != null ? commandLine.getCommandLineString() : "not constructed";
            LOGGER.debug("Finished `" + getPresentableName() + "` with an exception. Commandline: " +
                    commandLineInfo, e);
            return false;
        }
    }

    public void executeWithProgress(boolean modal) {
        //noinspection unchecked
        executeWithProgress(modal, Consumer.EMPTY_CONSUMER);
    }

    public void executeWithProgress(boolean modal, @NotNull Consumer<Boolean> consumer) {
        ProgressManager.getInstance().run(new Task.Backgroundable(myProject, getPresentableName(), true) {

            private boolean doNotStart;

            @Override
            public void onCancel() {
                doNotStart = true;
                ProcessHandler handler = getProcessHandler();
                if (handler != null) {
                    handler.destroyProcess();
                }
            }

            @Override
            public boolean shouldStartInBackground() {
                return !modal;
            }

            @Override
            public boolean isConditionalModal() {
                return modal;
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                if (doNotStart || myProject == null || myProject.isDisposed()) {
                    return;
                }
                indicator.setIndeterminate(true);
                consumer.consume(execute());
            }
        });
    }

    @Nullable
    public ProcessHandler getProcessHandler() {
        return myProcessHandler;
    }

    private void showNotification(@NotNull String message, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String title = getPresentableName();
            Notifications.Bus.notify(BallerinaConstants.BALLERINA_EXECUTION_NOTIFICATION_GROUP
                    .createNotification(title, message, type, null), myProject);
        });
    }

    private void showOutput(@NotNull OSProcessHandler originalHandler,
                            @NotNull BallerinaHistoryProcessListener historyProcessListener) {
        if (myShowOutputOnError) {
            BaseOSProcessHandler outputHandler = new KillableColoredProcessHandler(originalHandler.getProcess(), null);
            RunContentExecutor runContentExecutor = new RunContentExecutor(myProject, outputHandler)
                    .withTitle(getPresentableName())
                    .withActivateToolWindow(myShowOutputOnError)
                    .withFilter(new BallerinaConsoleFilter(myProject, myModule, myWorkDirectory != null ?
                            VfsUtilCore.pathToUrl(myWorkDirectory) : null));
            Disposer.register(myProject, runContentExecutor);
            runContentExecutor.run();
            historyProcessListener.apply(outputHandler);
        }
        if (myShowNotificationsOnError) {
            showNotification("Failed to run", NotificationType.ERROR);
        }
    }

    @NotNull
    public GeneralCommandLine createCommandLine() throws ExecutionException {
        GeneralCommandLine commandLine = !myPtyDisabled && PtyCommandLine.isEnabled() ?
                new PtyCommandLine() : new GeneralCommandLine();
        commandLine.setExePath(ObjectUtils.notNull(myExePath, ObjectUtils.notNull(BallerinaSdkService
                .getBallerinaExecutablePath(myBallerinaPath))));
        commandLine.getEnvironment().putAll(myExtraEnvironment);
        //Todo - Add BALLERINA_REPOSITORY
        //        commandLine.getEnvironment().put(BallerinaConstants.BALLERINA_REPOSITORY,
        //                StringUtil.notNullize(myBallerinaPath));

        Collection<String> paths = ContainerUtil.newArrayList();
        ContainerUtil.addIfNotNull(paths, StringUtil.nullize(commandLine.getEnvironment().get(
                BallerinaConstants.PATH), true));
        ContainerUtil.addIfNotNull(paths, StringUtil.nullize(EnvironmentUtil.getValue(BallerinaConstants.PATH), true));
        ContainerUtil.addIfNotNull(paths, StringUtil.nullize(myEnvPath, true));
        commandLine.getEnvironment().put(BallerinaConstants.PATH, StringUtil.join(paths, File.pathSeparator));

        commandLine.withWorkDirectory(myWorkDirectory);
        commandLine.addParameters(myParameterList.getList());
        commandLine.withParentEnvironmentType(myParentEnvironmentType);
        commandLine.withCharset(CharsetToolkit.UTF8_CHARSET);
        return commandLine;
    }

    @NotNull
    private String getPresentableName() {
        return ObjectUtils.notNull(myPresentableName, "Ballerina Executor");
    }
}
