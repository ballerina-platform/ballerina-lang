/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.IDLClientGeneratorResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.codeaction.CodeActionData;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.ResolvableCodeActionProvider;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Code Action for generating modules for client declarations.
 *
 * @since 2201.3.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class GenerateModuleForClientDeclCodeAction implements DiagnosticBasedCodeActionProvider, ResolvableCodeActionProvider {

    private static final String NAME = "Generate Client Declaration Module";

    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE4037");

    @Override
    public boolean validate(Diagnostic diagnostic,
                            DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        String uri = context.fileUri();
        CommandArgument posArg = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, range);
        CodeActionData codeActionData = new CodeActionData(getName(), uri, range, posArg);
        ResolvableCodeAction action = CodeActionUtil.createResolvableCodeAction(
                CommandConstants.GENERATE_MODULE_FOR_CLIENT_DECLARATION, CodeActionKind.QuickFix, codeActionData);
        action.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList((diagnostic))));
        return Collections.singletonList(action);
    }

    @Override
    public CodeAction resolve(ResolvableCodeAction codeAction, CodeActionResolveContext resolveContext) {
        String uri = codeAction.getData().getFileUri();
        Optional<Path> filePath = PathUtil.getPathFromURI(uri);
        if (filePath.isEmpty()) {
            throw new UserErrorException("Invalid file URI provided for the create function code action!");
        }

        SyntaxTree syntaxTree = resolveContext.workspace().syntaxTree(filePath.get()).orElseThrow();
        NonTerminalNode cursorNode = CommonUtil.findNode(codeAction.getData().getRange(), syntaxTree);
        if (cursorNode == null) {
            throw new UserErrorException("Failed to resolve code action");
        }
        Optional<Project> project = resolveContext.workspace().project(filePath.get());
        if (project.isEmpty()) {
            throw new UserErrorException("Failed to resolve project for provided file URI");
        }

        LSClientLogger clientLogger = LSClientLogger.getInstance(resolveContext.languageServercontext());
        ExtendedLanguageClient languageClient = resolveContext.getLanguageClient();
        String taskId = UUID.randomUUID().toString();
        CompletableFuture
                .runAsync(() -> {
                    clientLogger.logTrace("Generating modules for client declarations");

                    // Initialize progress notification
                    WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                    workDoneProgressCreateParams.setToken(taskId);
                    languageClient.createProgress(workDoneProgressCreateParams);

                    // Start progress
                    WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
                    beginNotification.setTitle(CommandConstants.GENERATE_MODULE_FOR_CLIENT_DECLARATION);
                    beginNotification.setCancellable(false);
                    beginNotification.setMessage("generation modules for client declarations");
                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                            Either.forLeft(beginNotification)));
                })
                //Todo: Check if we need to move provide resolution options to the project api side as in Pull module code action.
                .thenRunAsync(() -> {
                    IDLClientGeneratorResult idlClientGeneratorResult = project.get().currentPackage()
                            .runIDLGeneratorPlugins(ResolutionOptions.builder().setOffline(false).build());
                    boolean diagnosticNotResolved = idlClientGeneratorResult.reportedDiagnostics().diagnostics().stream().anyMatch(diagnostic ->
                            DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) || "BCE5303".equals(diagnostic.diagnosticInfo().code()));
                    if (diagnosticNotResolved) {
                        throw new UserErrorException("Failed to generate modules for client declarations");
                    }
                })
                .thenRunAsync(() -> {
                    DocumentServiceContext docContext = ContextBuilder.buildDocumentServiceContext(
                            filePath.get().toUri().toString(),
                            resolveContext.workspace(), LSContextOperation.TXT_DID_CHANGE,
                            resolveContext.languageServercontext());
                    DiagnosticsHelper.getInstance(resolveContext.languageServercontext())
                            .schedulePublishDiagnostics(languageClient, docContext);
                })
//                .thenRunAsync(() -> {
//                    try {
//                        // Refresh project
//                        ((BallerinaWorkspaceManager) resolveContext.workspace()).refreshProject(filePath.get());
//                    } catch (WorkspaceDocumentException e) {
//                        throw new UserErrorException("Failed to refresh project");
//                    }
//                })
                .whenComplete((result, throwable) -> {
                    boolean failed = false;
                    if (throwable != null) {
                        failed = true;
                        clientLogger.logError(LSContextOperation.TXT_RESOLVE_CODE_ACTION,
                                "Generate modules for client declarations failed : " + project.get().sourceRoot().toString(),
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
                                        + project.get().sourceRoot().toString());
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
        return codeAction;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
