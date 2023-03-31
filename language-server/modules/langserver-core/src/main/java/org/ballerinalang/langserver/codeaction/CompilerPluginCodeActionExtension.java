/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import com.google.gson.Gson;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContextImpl;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionExtension;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.codeaction.CodeActionData;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.util.LSClientUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Compiler plugin code action extension implementation for ballerina.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class CompilerPluginCodeActionExtension implements CodeActionExtension {

    private final Gson gson = new Gson();

    @Override
    public boolean validate(CodeActionParams inputParams) {
        // Here we check the .bal extension
        return inputParams.getTextDocument().getUri().endsWith(".bal");
    }

    @Override
    public List<? extends CodeAction> execute(CodeActionParams inputParams,
                                              org.ballerinalang.langserver.commons.CodeActionContext context,
                                              LanguageServerContext serverContext) {
        Optional<PackageCompilation> packageCompilation =
                context.workspace().waitAndGetPackageCompilation(context.filePath());
        if (packageCompilation.isEmpty() || context.currentDocument().isEmpty() ||
                context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }

        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        LSClientLogger clientLogger = LSClientLogger.getInstance(context.languageServercontext());

        // If code action resolve is not supported, we don't provide any code action
        if (!LSClientUtil.isCodeActionResolveSupported(clientCapabilities.getTextDocCapabilities())) {
            clientLogger.logTrace("Client doesn't support resolve code actions. Compiler plugin code actions disabled");
            return Collections.emptyList();
        }

        Position cursorPos = context.cursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPos.getLine(), cursorPos.getCharacter());
        Document document = context.currentDocument().get();
        SemanticModel semanticModel = context.currentSemanticModel().get();

        CodeActionManager codeActionManager = packageCompilation.get().getCodeActionManager();

        return context.diagnostics(context.filePath()).stream()
                .filter(diagnostic -> PositionUtil.isWithinRange(cursorPos,
                        PositionUtil.toRange(diagnostic.location().lineRange())))
                .map(diagnostic -> {
                    CodeActionContext codeActionContext = CodeActionContextImpl.from(context.fileUri(),
                            context.filePath(), linePosition, document, semanticModel, diagnostic);
                    return codeActionManager.codeActions(codeActionContext);
                })
                .peek(codeActionResult -> {
                    // Log all the errors captured while calculating code actions
                    codeActionResult.getErrors().forEach(ex -> {
                        clientLogger.logError(LSContextOperation.TXT_CODE_ACTION,
                                "Exception thrown while getting code action: '%s'" + ex.getCodeActionName(),
                                ex.getCause(), new TextDocumentIdentifier(context.fileUri()));
                    });
                })
                .flatMap(codeActionResult -> codeActionResult.getCodeActions().stream())
                .map(codeActionInfo -> {
                    // Provider name becomes the code action name when mapped here
                    CodeActionData codeActionData = new CodeActionData(codeActionInfo.getProviderName(),
                            context.fileUri(), new Range(cursorPos, cursorPos), codeActionInfo.getArguments());
                    return CodeActionUtil.createResolvableCodeAction(codeActionInfo.getTitle(),
                            CodeActionKind.QuickFix, codeActionData);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CodeAction resolve(ResolvableCodeAction codeAction, CodeActionResolveContext context) {
        Optional<PackageCompilation> packageCompilation =
                context.workspace().waitAndGetPackageCompilation(context.filePath());
        if (packageCompilation.isEmpty() || context.currentDocument().isEmpty() ||
                context.currentSemanticModel().isEmpty()) {
            return codeAction;
        }

        CodeActionData codeActionData = codeAction.getData();
        Optional<Path> filePath = PathUtil.getPathFromURI(codeActionData.getFileUri());
        if (filePath.isEmpty()) {
            return codeAction;
        }

        CodeActionManager codeActionManager = packageCompilation.get().getCodeActionManager();

        String providerName = codeActionData.getCodeActionName();
        // Code action data have to be converted to an array of code action arguments
        List<?> actionData = (List<?>) codeActionData.getActionData();
        List<CodeActionArgument> arguments = actionData.stream()
                .map(gson::toJsonTree)
                .map(CodeActionArgument::from)
                .collect(Collectors.toList());
        // Build the execution context and execute code action
        CodeActionExecutionContext codeActionContext = CodeActionExecutionContextImpl.from(
                codeActionData.getFileUri(),
                filePath.get(),
                PositionUtil.getLinePosition(codeActionData.getRange().getStart()),
                context.currentDocument().get(),
                context.currentSemanticModel().get(),
                arguments
        );
        List<DocumentEdit> docEdits = codeActionManager.executeCodeAction(providerName, codeActionContext);

        // Convert the returned edits into LSP4J edits
        List<Either<TextDocumentEdit, ResourceOperation>> edits = new LinkedList<>();
        docEdits.stream()
                .map(docEdit -> toTextDocumentEdit(docEdit, context))
                .filter(Optional::isPresent)
                .forEach(docEdit -> {
                    Either<TextDocumentEdit, ResourceOperation> either = Either.forLeft(docEdit.get());
                    edits.add(either);
                });

        codeAction.setEdit(new WorkspaceEdit(edits));
        return codeAction;
    }

    private Optional<TextDocumentEdit> toTextDocumentEdit(DocumentEdit docEdit, CodeActionResolveContext context) {
        Optional<SyntaxTree> originalST = PathUtil.getPathFromURI(docEdit.getFileUri())
                .flatMap(editedFilePath -> context.workspace().document(editedFilePath))
                .flatMap(doc -> Optional.of(doc.syntaxTree()));
        if (originalST.isEmpty()) {
            return Optional.empty();
        }

        TextRange textRange = originalST.get().rootNode().textRangeWithMinutiae();
        TextDocument textDocument = originalST.get().textDocument();
        LinePosition startPos = textDocument.linePositionFrom(textRange.startOffset());
        LinePosition endPos = textDocument.linePositionFrom(textRange.endOffset());
        LineRange lineRange = LineRange.from(originalST.get().filePath(), startPos, endPos);
        Range range = PositionUtil.toRange(LineRange.from(docEdit.getFileUri(),
                lineRange.startLine(), lineRange.endLine()));
        TextEdit edit = new TextEdit(range, docEdit.getModifiedSyntaxTree().toSourceCode());
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(
                docEdit.getFileUri(), null), Collections.singletonList(edit));
        return Optional.of(textDocumentEdit);
    }
}
