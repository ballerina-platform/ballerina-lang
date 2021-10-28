/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.ProjectLoader;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.task.BackgroundTaskService;
import org.ballerinalang.langserver.task.Task;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

/**
 * Command executor for pulling a package from central.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class PullModuleExecutor implements LSCommandExecutor {

    public static final String COMMAND = "PULL_MODULE";

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        String fileUri = null;
        String moduleName = null;
        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    fileUri = arg.valueAs(String.class);
                    break;
                case CommandConstants.ARG_KEY_MODULE_NAME:
                    moduleName = arg.valueAs(String.class);
                    break;
                default:
            }
        }

        Path filePath = CommonUtil.getPathFromURI(fileUri)
                .orElseThrow(() -> new UserErrorException("Couldn't determine file path"));

        Project project = context.workspace().project(filePath)
                .orElseThrow(() -> new UserErrorException("Couldn't find project to pull modules"));

        BackgroundTaskService taskService = BackgroundTaskService.getInstance(context.languageServercontext());
        if (taskService.isRunning(project.currentPackage().packageId(), PullModuleTask.NAME)) {
            CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Info,
                    "A pull modules operation is already running for this project");
        }
        taskService.submit(project.currentPackage().packageId(),
                new PullModuleTask(fileUri, project.sourceRoot(), context));

        return new Object();
    }

    protected static boolean isCancellation(Throwable t) {
        if (t instanceof CompletionException) {
            return isCancellation(t.getCause());
        }
        return (t instanceof CancellationException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * Task to perform pull module operation. This task will show a progress at vscode side and show success/error
     * messages accordingly.
     */
    static class PullModuleTask extends Task {

        static final String NAME = "PullModule";

        private final String fileUri;
        private final Path sourceRoot;
        private final ExecuteCommandContext context;

        public PullModuleTask(String fileUri, Path sourceRoot, ExecuteCommandContext context) {
            this.fileUri = fileUri;
            this.sourceRoot = sourceRoot;
            this.context = context;
        }

        @Override
        public void onStart() {
            LSClientLogger.getInstance(context.languageServercontext())
                    .logTrace("Started pulling modules for project: " + sourceRoot.toString());

            // Initialize progress notification
            WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
            workDoneProgressCreateParams.setToken(getTaskId());
            context.getLanguageClient().createProgress(workDoneProgressCreateParams);

            // Start progress
            WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
            beginNotification.setTitle("Pull Module");
            // TODO: Implement cancellation support in the future
            beginNotification.setCancellable(false);
            beginNotification.setMessage("pulling missing ballerina modules");
            context.getLanguageClient().notifyProgress(new ProgressParams(Either.forLeft(getTaskId()),
                    Either.forLeft(beginNotification)));
        }

        @Override
        public void execute() throws Exception {
            // Build the project in online mode
            BuildOptions options = new BuildOptionsBuilder().offline(false).build();
            Project project = ProjectLoader.loadProject(sourceRoot, options);
            // Pull modules and compile
            project.currentPackage().getCompilation();

            Path filePath = CommonUtil.getPathFromURI(fileUri)
                    .orElseThrow(() -> new ProjectException("Couldn't determine the project path"));
            // Reload project
            ((BallerinaWorkspaceManager) context.workspace()).refreshProject(filePath);

            DocumentServiceContext documentServiceContext = ContextBuilder.buildDocumentServiceContext(fileUri,
                    context.workspace(), LSContextOperation.TXT_DID_CHANGE,
                    context.languageServercontext());
            DiagnosticsHelper diagnosticsHelper = DiagnosticsHelper.getInstance(context.languageServercontext());
            diagnosticsHelper.compileAndSendDiagnostics(context.getLanguageClient(), documentServiceContext);
        }

        @Override
        public void onSuccess() {
            WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
            endNotification.setMessage("Modules pulled successfully!");
            context.getLanguageClient().notifyProgress(new ProgressParams(Either.forLeft(getTaskId()),
                    Either.forLeft(endNotification)));

            CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Info,
                    "Module(s) pulled successfully!");

            LSClientLogger.getInstance(context.languageServercontext())
                    .logTrace("Finished pulling modules for project: " + sourceRoot.toString());
        }

        @Override
        public void onFail(Throwable t) {
            WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
            endNotification.setMessage("Unable to pull modules. Aborted!");
            context.getLanguageClient().notifyProgress(new ProgressParams(Either.forLeft(getTaskId()),
                    Either.forLeft(endNotification)));

            LSClientLogger.getInstance(context.languageServercontext()).logError(LSContextOperation.WS_EXEC_CMD,
                    "Pull modules failed for project: " + sourceRoot.toString(),
                    t, null, (Position) null);

            if (isCancellation(t)) {
                CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Error,
                        "Pull modules operation cancelled");
            } else if (t instanceof ProjectException) {
                CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Error,
                        "Error occurred while pulling modules");
            } else {
                CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Error,
                        "Failed to pull modules!");
            }
        }

        @Override
        public String name() {
            return NAME;
        }
    }
}
