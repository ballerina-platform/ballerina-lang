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
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.eclipse.lsp4j.MessageType;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Command executor for pulling a package from central.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class PullModuleExecutor implements LSCommandExecutor {

    public static final String COMMAND = "PULL_MODULE";

    private final ExecutorService executor;
    private final Map<Path, CompletableFuture<Void>> tasks = new ConcurrentHashMap<>();

    public PullModuleExecutor() {
        executor = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Cancel all futures
            tasks.forEach((sourceRoot, future) -> {
                if (!future.isDone() || !future.isCancelled()) {
                    future.cancel(true);
                }
            });
            tasks.clear();
            executor.shutdownNow();
        }));
    }

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

        Optional<Project> existingProject = CommonUtil.getPathFromURI(fileUri)
                .flatMap(filePath -> context.workspace().project(filePath));
        if (existingProject.isEmpty()) {
            throw new UserErrorException("Couldn't find project to pull modules");
        }

        Path sourceRoot = existingProject.get().sourceRoot();

        if (tasks.containsKey(sourceRoot)) {
            CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Info,
                    "A pull modules operation is already running for this project");
            return new Object();
        }

        LSClientLogger.getInstance(context.languageServercontext())
                .logTrace("Pulling modules for project: " + sourceRoot.toString());

        PullModuleTask pullModuleTask = new PullModuleTask(fileUri, sourceRoot, context);
        pullModuleTask.run();

        LSClientLogger.getInstance(context.languageServercontext())
                .logTrace("Finished pulling modules for project: " + sourceRoot.toString());
        
//        CompletableFuture<Void> future = CompletableFuture.runAsync(new PullModuleTask(fileUri, sourceRoot, context));
//        tasks.put(sourceRoot, future);
//        future.thenAccept(v -> {
//            CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Info,
//                    "Module(s) pulled successfully!");
//            LSClientLogger.getInstance(context.languageServercontext())
//                    .logTrace("Finished pulling modules for project: " + sourceRoot.toString());
//        }).exceptionally(t -> {
//            LSClientLogger.getInstance(context.languageServercontext())
//                    .logError(LSContextOperation.WS_EXEC_CMD,
//                            "Pull modules failed for project: " + sourceRoot.toString(),
//                            t, null, (Position) null);
//
//            if (isCancellation(t)) {
//                CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Error,
//                        "Pull modules operation cancelled");
//            } else if (t instanceof ProjectException) {
//                CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Error,
//                        "Error occurred while pulling modules");
//            } else {
//                CommandUtil.notifyClient(context.getLanguageClient(), MessageType.Error,
//                        "Failed to pull modules!");
//            }
//            return null;
//        }).thenApply(v -> {
//            tasks.remove(sourceRoot);
//            return null;
//        });

        return new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }

    protected boolean isCancellation(Throwable t) {
        if (t instanceof CompletionException) {
            return isCancellation(t.getCause());
        }
        return (t instanceof CancellationException);
    }

    static class PullModuleTask implements Runnable {

        private final String fileUri;
        private final Path sourceRoot;
        private final ExecuteCommandContext context;

        public PullModuleTask(String fileUri, Path sourceRoot, ExecuteCommandContext context) {
            this.fileUri = fileUri;
            this.sourceRoot = sourceRoot;
            this.context = context;
        }

        @Override
        public void run() {
            String offlineProp = System.getProperty(ProjectConstants.BALLERINA_OFFLINE_FLAG);
            try {
                // Build the project in online mode
                BuildOptions options = new BuildOptionsBuilder().offline(false).build();
                Project project = ProjectLoader.loadProject(sourceRoot, options);
                // Pull modules and compile
                project.currentPackage().getCompilation();
            } finally {
                System.setProperty(ProjectConstants.BALLERINA_OFFLINE_FLAG, offlineProp);
            }

            Path filePath = CommonUtil.getPathFromURI(fileUri)
                    .orElseThrow(() -> new ProjectException("Couldn't determine the project path"));
            // Reload project
//            context.workspace().reloadProject(filePath);

//            DocumentServiceContext documentServiceContext = ContextBuilder.buildBaseContext(fileUri,
//                    context.workspace(), LSContextOperation.TXT_DID_CHANGE,
//                    context.languageServercontext());
//            DiagnosticsHelper diagnosticsHelper = DiagnosticsHelper.getInstance(context.languageServercontext());
//            diagnosticsHelper.compileAndSendDiagnostics(context.getLanguageClient(), documentServiceContext);
        }
    }
}
