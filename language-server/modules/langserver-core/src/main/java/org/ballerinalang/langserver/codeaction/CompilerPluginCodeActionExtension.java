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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.CodeActionResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionContextImpl;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionExtension;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Compiler plugin code action extension implementation for ballerina.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class CompilerPluginCodeActionExtension implements CodeActionExtension {

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

        Position position = context.cursorPosition();
        LinePosition linePosition = LinePosition.from(position.getLine(), position.getCharacter());
        Document document = context.currentDocument().get();
        SemanticModel semanticModel = context.currentSemanticModel().get();

        CodeActionManager codeActionManager = packageCompilation.get().getCodeActionManager();

        List<CodeAction> codeActions = new LinkedList<>();

        context.diagnostics(context.filePath()).stream()
                .filter(diag -> CommonUtil.isWithinRange(context.cursorPosition(),
                        CommonUtil.toRange(diag.location().lineRange())))
                .forEach(diagnostic -> {
                    CodeActionContext codeActionContext = CodeActionContextImpl.from(context.fileUri(),
                            context.filePath(), linePosition, document, semanticModel, diagnostic);
                    CodeActionResult result = codeActionManager.codeActions(codeActionContext);
                    result.getCodeActions().stream()
                            .map(codeActionCommand -> {
                                CodeAction action = new CodeAction(codeActionCommand.getTitle());

                                List<Object> arguments = new LinkedList<>();
                                arguments.add(CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI,
                                        context.fileUri()));
                                arguments.addAll(codeActionCommand.getArguments());
                                action.setCommand(new Command(codeActionCommand.getTitle(),
                                        codeActionCommand.getProviderName(), arguments));
                                return action;
                            })
                            .forEach(codeActions::add);

                    // Log all the errors captured while calculating codeactions
                    result.getErrors().forEach(ex -> {
                        LSClientLogger.getInstance(context.languageServercontext())
                                .logError(LSContextOperation.TXT_CODE_ACTION,
                                        "Exception thrown while getting code action: '%s'" + ex.getCodeActionName(),
                                        ex.getCause(), new TextDocumentIdentifier(context.fileUri()));
                    });
                });

        return codeActions;
    }
}
