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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
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
    private static final String DIAGNOSTIC_CODE_3961 = "BCE3961";
    private static final String ANONYMOUS_FUNCTION_EXPRESSION = "Anonymous function expression";
    private static final Set<String> DIAGNOSTIC_CODES =
            Set.of("BCE3946", "BCE3947", "BCE3950", DIAGNOSTIC_CODE_3961);

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
        NonTerminalNode nonTerminalNode = positionDetails.matchedNode();

        // Check if the diagnostic is for an anonymous function expression
        if (nonTerminalNode.kind() == SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION) {
            ExplicitAnonymousFunctionExpressionNode functionExpressionNode =
                    (ExplicitAnonymousFunctionExpressionNode) nonTerminalNode;
            return getCodeAction(functionExpressionNode.functionKeyword(), ANONYMOUS_FUNCTION_EXPRESSION,
                    context.fileUri());
        }

        // Check if there are multiple diagnostics of the considered category
        List<Diagnostic> diagnostics = context.diagnostics(context.filePath());
        if (diagnostics.size() > 1 && hasMultipleDiagnostics(nonTerminalNode, diagnostic, diagnostics)) {
            return Collections.emptyList();
        }

        // Obtain the symbol of the referred function
        Optional<Symbol> symbol = getReferredSymbol(context, nonTerminalNode);
        if (symbol.isEmpty() || symbol.get().getModule().isEmpty()) {
            return Collections.emptyList();
        }

        // Obtain the current project
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }

        // Obtain the file path of the referred symbol
        Optional<Path> filePath = PathUtil.getFilePathForSymbol(symbol.get(), project.get(), context);
        if (filePath.isEmpty() || context.workspace().syntaxTree(filePath.get()).isEmpty()) {
            return Collections.emptyList();
        }

        // Obtain the node of the referred symbol
        Optional<NonTerminalNode> node = CommonUtil.findNode(symbol.get(),
                context.workspace().syntaxTree(filePath.get()).get());
        if (node.isEmpty() || isUnsupportedSyntaxKind(node.get().kind())) {
            return Collections.emptyList();
        }
        FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node.get();

        return getCodeAction(functionDefinitionNode.functionKeyword(), symbol.get().getName().orElse(""),
                filePath.get().toUri().toString());
    }

    private static Optional<Symbol> getReferredSymbol(CodeActionContext context, NonTerminalNode node) {
        SyntaxKind kind = node.kind();
        if (kind == SyntaxKind.EXPLICIT_NEW_EXPRESSION || kind == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            try {
                TypeReferenceTypeSymbol typeSymbol = (TypeReferenceTypeSymbol) context.currentSemanticModel()
                        .flatMap(semanticModel -> semanticModel.typeOf(node))
                        .orElseThrow();
                ClassSymbol definition = (ClassSymbol) typeSymbol.definition();
                return Optional.of(definition.initMethod().orElseThrow());
            } catch (RuntimeException e) {
                assert false : "This line is unreachable because the diagnostic is not produced otherwise.";
                return Optional.empty();
            }
        }
        return context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(node));
    }

    private static List<CodeAction> getCodeAction(Token functionKeyword, String expressionName, String filePath) {
        Position position = PositionUtil.toPosition(functionKeyword.lineRange().startLine());
        String editText = SyntaxKind.ISOLATED_KEYWORD.stringValue() + " ";
        TextEdit textEdit = new TextEdit(new Range(position, position), editText);
        String commandTitle = String.format(CommandConstants.MAKE_FUNCTION_ISOLATE, expressionName);
        return Collections.singletonList(
                CodeActionUtil.createCodeAction(commandTitle, List.of(textEdit), filePath, CodeActionKind.QuickFix));
    }

    private static boolean isUnsupportedSyntaxKind(SyntaxKind kind) {
        return kind != SyntaxKind.FUNCTION_DEFINITION && kind != SyntaxKind.OBJECT_METHOD_DEFINITION;
    }

    private static boolean hasMultipleDiagnostics(NonTerminalNode node, Diagnostic currentDiagnostic,
                                                  List<Diagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(diagnostic -> !currentDiagnostic.equals(diagnostic) &&
                DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) &&
                PositionUtil.isWithinLineRange(diagnostic.location().lineRange(), node.lineRange())) &&
                currentDiagnostic.diagnosticInfo().code().equals(DIAGNOSTIC_CODE_3961);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
