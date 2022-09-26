package org.ballerinalang.langserver.command.executors;

import io.ballerina.projects.IDLClientGeneratorResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.ResolutionOptions;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Command executor for generating modules for client declaration.
 *
 * @since 2201.3.0
 */
public class GenerateModuleForClientDeclExecutor implements LSCommandExecutor {

    public static final String COMMAND = "GENERATE_MODULE_FOR_CLIENT_DECL";

    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE4037", "BCE5303");

    @Override
    public Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        String fileUri = null;
        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    fileUri = arg.valueAs(String.class);
                    break;
            }
        }

        String taskId = UUID.randomUUID().toString();
        Path filePath = PathUtil.getPathFromURI(fileUri)
                .orElseThrow(() -> new UserErrorException("Couldn't determine file path"));

        Project project = context.workspace().project(filePath)
                .orElseThrow(() -> new UserErrorException("Failed to resolve project for provided file URI"));

        ExtendedLanguageClient languageClient = context.getLanguageClient();
        LSClientLogger clientLogger = LSClientLogger.getInstance(context.languageServercontext());

        CompletableFuture
                .runAsync(() -> {
                    clientLogger.logTrace("Generating modules for client declarations");

                    // Initialize progress notification
                    WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                    workDoneProgressCreateParams.setToken(taskId);
                    languageClient.createProgress(workDoneProgressCreateParams);

                    // Start progress
                    WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
                    beginNotification.setTitle(CommandConstants.GENERATE_MODULE_FOR_CLIENT_DECLARATION_TITLE);
                    beginNotification.setCancellable(false);
                    beginNotification.setMessage("Generating modules for client declarations");
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(beginNotification)));
                })
                .thenRunAsync(() -> {
                    IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage()
                            .runIDLGeneratorPlugins(ResolutionOptions.builder().setOffline(false).build());
                    boolean diagnosticNotResolved = idlClientGeneratorResult.reportedDiagnostics().diagnostics().stream().anyMatch(diagnostic ->
                            DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()));
                    if (diagnosticNotResolved) {
                        throw new UserErrorException("Failed to generate modules for client declarations");
                    }
                })
                .thenRunAsync(() -> {
                    try {
                        // Refresh project
                        ((BallerinaWorkspaceManager) context.workspace()).refreshProject(filePath);
                    } catch (WorkspaceDocumentException e) {
                        throw new UserErrorException("Failed to refresh project");
                    }
                })
                .thenRunAsync(() -> {
                    DocumentServiceContext docContext = ContextBuilder.buildDocumentServiceContext(
                            filePath.toUri().toString(),
                            context.workspace(), LSContextOperation.TXT_DID_CHANGE,
                            context.languageServercontext());
                    DiagnosticsHelper.getInstance(context.languageServercontext())
                            .schedulePublishDiagnostics(languageClient, docContext);
                })
                .whenComplete((result, throwable) -> {
                    boolean failed = false;
                    if (throwable != null) {
                        failed = true;
                        clientLogger.logError(LSContextOperation.TXT_RESOLVE_CODE_ACTION,
                                "Generate modules for client declarations failed : " + project.sourceRoot().toString(),
                                throwable, null, (Position) null);
                        if (throwable.getCause() instanceof UserErrorException) {
                            String errorMessage = throwable.getCause().getMessage();
                            CommandUtil.notifyClient(languageClient, MessageType.Error, errorMessage);
                        } else {
                            CommandUtil.notifyClient(languageClient, MessageType.Error, "Failed to generate modules for " +
                                    "client declarations");
                        }
                    } else {
                        CommandUtil
                                .notifyClient(languageClient, MessageType.Info,
                                        "Modules for client declarations generated successfully!");
                        clientLogger
                                .logTrace("Finished generating modules for client declarations "
                                        + project.sourceRoot().toString());
                    }
                    WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
                    if (failed) {
                        endNotification.setMessage("Failed to generate modules for client declarations!");
                    } else {
                        endNotification.setMessage("Generated modules for client declarations successfully!");
                    }
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(endNotification)));

                });
        return new Object();
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
