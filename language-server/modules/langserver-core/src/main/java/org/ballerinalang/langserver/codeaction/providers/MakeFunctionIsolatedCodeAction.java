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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action to make function isolate.
 *
 * @since 2201.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MakeFunctionIsolatedCodeAction extends AbstractCodeActionProvider {
    public static final String NAME = "Make Function Isolated";
    public static final String DIAGNOSTIC_CODE_BCE3946 = "BCE3946";
    public static final String DIAGNOSTIC_CODE_BCE3947 = "BCE3947";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!DIAGNOSTIC_CODE_BCE3946.equals(diagnostic.diagnosticInfo().code()) 
                && !DIAGNOSTIC_CODE_BCE3947.equals(diagnostic.diagnosticInfo().code()) 
                || context.currentSyntaxTree().isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }
        Range diagnosticRange = CommonUtil.toRange(diagnostic.location().lineRange());
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
        Optional<Path> filePath = CommonUtil.getFilePathForSymbol(symbol.get(), project.get(), context);
        if (filePath.isEmpty() || context.workspace().syntaxTree(filePath.get()).isEmpty()) {
            return Collections.emptyList();
        }
        URI uri = filePath.get().toUri();

        Optional<NonTerminalNode> node = CommonUtil.findNode(symbol.get(), 
                context.workspace().syntaxTree(filePath.get()).get());
        if (node.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Position> position = getStartPosition(node.get());
        if (position.isEmpty()) {
            return Collections.emptyList();
        }
        Range range = new Range(position.get(), position.get());
        String editText = SyntaxKind.ISOLATED_KEYWORD.stringValue() + " ";
        TextEdit textEdit = new TextEdit(range, editText);
        List<TextEdit> editList = List.of(textEdit);
        String commandTitle = String.format(CommandConstants.MAKE_FUNCTION_ISOLATE, symbol.get().getName().orElse(""));
        return Collections.singletonList(createCodeAction(commandTitle, editList, uri.toString(), 
                CodeActionKind.QuickFix));
    }

    private List<CodeAction> getExplicitAnonFuncExpressionCodeAction(ExplicitAnonymousFunctionExpressionNode node, 
                                                                     CodeActionContext context) {
        Position position = CommonUtil.toPosition(node.functionKeyword().lineRange().startLine());
        Range range = new Range(position, position);
        String editText = SyntaxKind.ISOLATED_KEYWORD.stringValue() + " ";
        TextEdit textEdit = new TextEdit(range, editText);
        List<TextEdit> editList = List.of(textEdit);
        String commandTitle = String.format(CommandConstants.MAKE_FUNCTION_ISOLATE, "Anonymous function expression");
        return Collections.singletonList(createCodeAction(commandTitle, editList, context.fileUri(),
                CodeActionKind.QuickFix));
    }

    private static Optional<Position> getStartPosition(Node node) {
        SyntaxKind typeKind = node.kind();
        if (typeKind.equals(SyntaxKind.FUNCTION_DEFINITION)) {
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
            return Optional.of(CommonUtil.toPosition(functionDefinitionNode.functionKeyword().lineRange().startLine()));
        }
        return Optional.empty();
    }

    @Override
    public int priority() {
        return super.priority();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
