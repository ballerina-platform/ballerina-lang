/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.docs;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.command.executors.UpdateDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for adding single documentation.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class UpdateDocumentationCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Update Documentation";

    private static final List<String> DIAGNOSTIC_IDS = Arrays.asList(
            DiagnosticWarningCode.UNDOCUMENTED_PARAMETER.diagnosticId(),
            DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_PARAMETER.diagnosticId(),
            DiagnosticWarningCode.PARAMETER_ALREADY_DOCUMENTED.diagnosticId(),
            DiagnosticWarningCode.UNDOCUMENTED_FIELD.diagnosticId(),
            DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_FIELD.diagnosticId(),
            DiagnosticWarningCode.FIELD_ALREADY_DOCUMENTED.diagnosticId(),
            DiagnosticWarningCode.UNDOCUMENTED_VARIABLE.diagnosticId(),
            DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_VARIABLE.diagnosticId(),
            DiagnosticWarningCode.VARIABLE_ALREADY_DOCUMENTED.diagnosticId(),
            DiagnosticWarningCode.UNDOCUMENTED_RETURN_PARAMETER.diagnosticId(),
            DiagnosticWarningCode.NO_DOCUMENTABLE_RETURN_PARAMETER.diagnosticId(),
            DiagnosticWarningCode.INVALID_DOCUMENTATION_REFERENCE.diagnosticId(),
            DiagnosticWarningCode.INVALID_USAGE_OF_PARAMETER_REFERENCE.diagnosticId(),
            DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_ATTRIBUTE.diagnosticId(),
            DiagnosticWarningCode.INVALID_USE_OF_ENDPOINT_DOCUMENTATION_ATTRIBUTE.diagnosticId(),
            DiagnosticWarningCode.DUPLICATE_DOCUMENTED_ATTRIBUTE.diagnosticId(),
            DiagnosticWarningCode.USAGE_OF_DEPRECATED_CONSTRUCT.diagnosticId(),
            DiagnosticWarningCode.DEPRECATION_DOCUMENTATION_SHOULD_BE_AVAILABLE.diagnosticId());

    @Override
    public boolean isEnabled(LanguageServerContext serverContext) {
        return false;
    }

    @Override
    public int priority() {
        return 998;
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {

        String code = diagnostic.diagnosticInfo().code();
        return DIAGNOSTIC_IDS.stream().anyMatch(id -> id.equals(code)) &&
                CodeActionNodeValidator.validate(context.nodeAtCursor());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {

        String docUri = context.fileUri();
        SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
        Optional<NonTerminalNode> topLevelNode = CodeActionUtil.getTopLevelNode(context.range(), syntaxTree);
        if (topLevelNode.isEmpty()) {
            return Collections.emptyList();
        }

        // TODO: #27493 Documenting services is not fully supported yet due to a limitation in semantic API
        if (topLevelNode.get().kind() == SyntaxKind.SERVICE_DECLARATION) {
            return Collections.emptyList();
        }

        NonTerminalNode node = topLevelNode.get();
        if (node.kind() == SyntaxKind.MARKDOWN_DOCUMENTATION) {
            // If diagnostic message positions inside docs, get parent() node
            node = node.parent().parent();
        }
        CommandArgument docUriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE,
                PositionUtil.toRange(node.lineRange()));
        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, lineStart));

        String commandTitle = CommandConstants.UPDATE_DOCUMENTATION_TITLE;
        Command command = new Command(commandTitle, UpdateDocumentationExecutor.COMMAND, args);
        CodeAction action = CodeActionUtil.createCodeAction(commandTitle, command, CodeActionKind.QuickFix);
        action.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList(diagnostic)));
        return Collections.singletonList(action);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
