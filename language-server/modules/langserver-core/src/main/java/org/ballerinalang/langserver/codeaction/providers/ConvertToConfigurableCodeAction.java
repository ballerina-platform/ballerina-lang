/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;
import java.util.Optional;

/**
 * Code Action to convert module variables to configurable variable.
 *
 * @since 2201.10.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ConvertToConfigurableCodeAction implements RangeBasedCodeActionProvider {

    private static final String CONFIGURABLE = "configurable";
    private static final List<String> LIST_UPDATING_METHODS = List.of("push", "pop", "remove", "removeAll", "reverse",
            "setLength", "shift", "slice", "unshift");
    private static final List<String> TABLE_UPDATING_METHODS = List.of("add", "put", "remove", "removeAll",
            "removeIfHasKey");
    private static final List<String> MAPPING_UPDATING_METHODS = List.of("remove", "removeAll", "removeIfHasKey");

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.BOOLEAN_LITERAL, SyntaxKind.NUMERIC_LITERAL, SyntaxKind.STRING_LITERAL,
                SyntaxKind.UNARY_EXPRESSION, SyntaxKind.XML_TEMPLATE_EXPRESSION, SyntaxKind.STRING_TEMPLATE_EXPRESSION,
                SyntaxKind.MAPPING_CONSTRUCTOR, SyntaxKind.LIST_CONSTRUCTOR, SyntaxKind.TABLE_CONSTRUCTOR);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        NonTerminalNode matchedNode = positionDetails.matchedCodeActionNode();
        NonTerminalNode parentNode = matchedNode.parent();
        if (parentNode.kind() != SyntaxKind.MODULE_VAR_DECL
                || !CodeActionNodeValidator.validate(context.nodeAtRange())) {
            return false;
        }
        ModuleVariableDeclarationNode modVarDeclarationNode = (ModuleVariableDeclarationNode) parentNode;
        if (modVarDeclarationNode.typedBindingPattern().typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC
                || hasIsolatedOrFinalOrConfigurableQualifier(modVarDeclarationNode)) {
            return false;
        }
        SemanticModel semanticModel = context.currentSemanticModel().get();
        Optional<Symbol> symbol = semanticModel.symbol(modVarDeclarationNode);
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.VARIABLE) {
            return false;
        }

        VariableSymbol variableSymbol = (VariableSymbol) symbol.get();

        List<Location> references = semanticModel.references(variableSymbol);
        NonTerminalNode rootNode = context.currentSyntaxTree().get().rootNode();

        if (isVariableAssigned(references, rootNode)) {
            return false;
        }

        TypeSymbol varTypeSymbol = variableSymbol.typeDescriptor();
        Types types = semanticModel.types();
        if (!varTypeSymbol.subtypeOf(types.ANYDATA) || types.REGEX.subtypeOf(varTypeSymbol)) {
            return false;
        }

        if (!variableSymbol.typeDescriptor().subtypeOf(semanticModel.types().READONLY)) {
            switch (matchedNode.kind()) {
                case LIST_CONSTRUCTOR -> {
                    return !isListVarUpdated(references, rootNode);
                }
                case MAPPING_CONSTRUCTOR -> {
                    return !isMappingVarUpdated(references, rootNode);
                }
                case TABLE_CONSTRUCTOR -> {
                    return !isTableVarUpdated(references, rootNode);
                }
            }
        }

        return true;
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        ModuleVariableDeclarationNode modVarDeclarationNode =
                (ModuleVariableDeclarationNode) posDetails.matchedCodeActionNode().parent();

        TextEdit textEdit = modVarDeclarationNode.visibilityQualifier()
                .map(token ->
                        new TextEdit(PositionUtil.toRange(token.lineRange().endLine()), " " + CONFIGURABLE))
                .orElseGet(() ->
                        new TextEdit(PositionUtil.toRange(modVarDeclarationNode.lineRange().startLine()),
                                CONFIGURABLE + " "));

        CodeAction codeAction =  CodeActionUtil.createCodeAction(CommandConstants.CONVERT_TO_CONFIGURABLE,
                List.of(textEdit), context.fileUri(), CodeActionKind.Empty);
        return List.of(codeAction);
    }

    private static boolean hasIsolatedOrFinalOrConfigurableQualifier(
            ModuleVariableDeclarationNode modVarDeclarationNode) {
        return modVarDeclarationNode.qualifiers()
                .stream().anyMatch(q -> q.text().equals(Qualifier.ISOLATED.getValue())
                        || q.text().equals(Qualifier.FINAL.getValue())
                        || q.text().equals(Qualifier.CONFIGURABLE.getValue()));
    }

    private static boolean isVariableAssigned(List<Location> references, NonTerminalNode rootNode) {
        return references.stream()
                .map(location -> rootNode.findNode(location.textRange()))
                .anyMatch(node -> node.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT);
    }

    private static boolean isListVarUpdated(List<Location> references, NonTerminalNode rootNode) {
        for (Location location : references) {
            NonTerminalNode node = rootNode.findNode(location.textRange());
            NonTerminalNode parentNode = node.parent();
            SyntaxKind parentKind = parentNode.kind();
            if (parentKind == SyntaxKind.INDEXED_EXPRESSION
                    && parentNode.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
                return true;
            }
            if (parentKind == SyntaxKind.METHOD_CALL) {
                if (LIST_UPDATING_METHODS.contains(((MethodCallExpressionNode) parentNode)
                        .methodName().toSourceCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTableVarUpdated(List<Location> references, NonTerminalNode rootNode) {
        for (Location location : references) {
            NonTerminalNode node = rootNode.findNode(location.textRange());
            NonTerminalNode parentNode = node.parent();
            if (parentNode.kind() == SyntaxKind.METHOD_CALL) {
                if (TABLE_UPDATING_METHODS.contains(((MethodCallExpressionNode) parentNode)
                        .methodName().toSourceCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isMappingVarUpdated(List<Location> references, NonTerminalNode rootNode) {
        for (Location location : references) {
            NonTerminalNode node = rootNode.findNode(location.textRange());
            if (node.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
                ExpressionNode exprNode = ((AssignmentStatementNode) node).expression();
                if (exprNode.kind() == SyntaxKind.METHOD_CALL) {
                    MethodCallExpressionNode methodCallExpressionNode = (MethodCallExpressionNode) exprNode;
                    return MAPPING_UPDATING_METHODS.contains(methodCallExpressionNode.methodName().toSourceCode());
                }
                return false;
            }
            NonTerminalNode parentNode = node.parent();
            if (parentNode.kind() == SyntaxKind.INDEXED_EXPRESSION
                    && parentNode.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return CommandConstants.CONVERT_TO_CONFIGURABLE;
    }
}
