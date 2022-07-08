/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code Action to add isolated qualifier.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddIsolatedQualifierCodeAction implements DiagnosticBasedCodeActionProvider {
    public static final String NAME = "Add Isolated Qualifier";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE3946", "BCE3947");

    @Override
    public boolean validate(Diagnostic diagnostic,
                            DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange())
                && context.currentSyntaxTree().isPresent()
                && context.currentSemanticModel().isPresent();
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Range diagnosticRange = PositionUtil.toRange(diagnostic.location().lineRange());
        // isPresent() check is done in the validator
        NonTerminalNode nonTerminalNode = CommonUtil.findNode(diagnosticRange, context.currentSyntaxTree().get());

        if (nonTerminalNode.kind() == SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION) {
            return getExplicitAnonFuncExpressionCodeAction((ExplicitAnonymousFunctionExpressionNode) nonTerminalNode,
                    context);
        }
        Optional<Symbol> symbol = context.currentSemanticModel().get().symbol(nonTerminalNode);
        if (symbol.isEmpty() || symbol.get().getModule().isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<Path> filePath = PathUtil.getFilePathForSymbol(symbol.get(), project.get(), context);
        if (filePath.isEmpty() || context.workspace().syntaxTree(filePath.get()).isEmpty()) {
            return Collections.emptyList();
        }

        Optional<NonTerminalNode> node = CommonUtil.findNode(symbol.get(),
                context.workspace().syntaxTree(filePath.get()).get());
        if (node.isEmpty() || !node.get().kind().equals(SyntaxKind.FUNCTION_DEFINITION)) {
            return Collections.emptyList();
        }

        FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node.get();
        Position position = PositionUtil.toPosition(functionDefinitionNode.functionKeyword().lineRange().startLine());

        Range range = new Range(position, position);
        String editText = SyntaxKind.ISOLATED_KEYWORD.stringValue() + " ";
        TextEdit textEdit = new TextEdit(range, editText);
        List<TextEdit> editList = List.of(textEdit);
        String commandTitle = String.format(CommandConstants.MAKE_FUNCTION_ISOLATE, symbol.get().getName().orElse(""));
        return Collections.singletonList(CodeActionUtil
                .createCodeAction(commandTitle, editList, filePath.get().toUri().toString(), CodeActionKind.QuickFix));
    }

    private List<CodeAction> getExplicitAnonFuncExpressionCodeAction(ExplicitAnonymousFunctionExpressionNode node,
                                                                     CodeActionContext context) {
        Position position = PositionUtil.toPosition(node.functionKeyword().lineRange().startLine());
        Range range = new Range(position, position);
        String editText = SyntaxKind.ISOLATED_KEYWORD.stringValue() + " ";
        TextEdit textEdit = new TextEdit(range, editText);
        List<TextEdit> editList = List.of(textEdit);
        String commandTitle = String.format(CommandConstants.MAKE_FUNCTION_ISOLATE, "Anonymous function expression");
        return Collections.singletonList(CodeActionUtil.createCodeAction(commandTitle, editList, context.fileUri(),
                CodeActionKind.QuickFix));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
