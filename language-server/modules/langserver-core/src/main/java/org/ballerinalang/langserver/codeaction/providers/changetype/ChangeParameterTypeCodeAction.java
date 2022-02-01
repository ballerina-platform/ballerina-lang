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
package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code Action for change parameter type.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ChangeParameterTypeCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Change Parameter Type";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068");

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        // Skip, non-local var declarations
        VariableDeclarationNode localVarNode = getVariableDeclarationNode(positionDetails.matchedNode());
        if (localVarNode == null) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> typeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED_SYMBOL_INDEX);
        if (typeSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        // Skip, variable declarations with non-initializers
        Optional<ExpressionNode> initializer = localVarNode.initializer();
        if (initializer.isEmpty()) {
            return Collections.emptyList();
        }

        // Get parameter symbol
        SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
        Optional<Symbol> optParamSymbol = semanticModel.symbol(initializer.get());
        if (optParamSymbol.isEmpty() || optParamSymbol.get().kind() != SymbolKind.PARAMETER) {
            return Collections.emptyList();
        }
        ParameterSymbol paramSymbol = (ParameterSymbol) optParamSymbol.get();

        // TODO: Check whether the following code segment is required. Since we know the symbol is a param symbol, it
        //  follows that the var decl is within a function/method.
        // Find parent function definition
        Optional<FunctionDefinitionNode> funcDefNode = CodeActionUtil.getEnclosedFunction(localVarNode);
        if (funcDefNode.isEmpty()) {
            return Collections.emptyList();
        }

        // Get line-range of type-desc of parameter
        SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
        Optional<NonTerminalNode> node = CommonUtil.findNode(paramSymbol, syntaxTree);
        if (node.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<Range> paramTypeRange = getParameterTypeRange(node.get());
        if (paramTypeRange.isEmpty()) {
            return Collections.emptyList();
        }

        // Derive possible types
        List<CodeAction> actions = new ArrayList<>();
        List<TextEdit> importEdits = new ArrayList<>();
        List<String> types = CodeActionUtil.getPossibleTypes(typeSymbol.get(), importEdits, context);
        for (String type : types) {
            List<TextEdit> edits = new ArrayList<>();
            edits.add(new TextEdit(paramTypeRange.get(), type));
            String commandTitle = String.format(CommandConstants.CHANGE_PARAM_TYPE_TITLE, paramSymbol.getName().get(),
                                                type);
            actions.add(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
        }
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private VariableDeclarationNode getVariableDeclarationNode(NonTerminalNode node) {
        while (node != null && node.kind() != SyntaxKind.LOCAL_VAR_DECL) {
            node = node.parent();
        }

        return node != null ? (VariableDeclarationNode) node : null;
    }

    private Optional<Range> getParameterTypeRange(NonTerminalNode parameterNode) {
        if (parameterNode == null) {
            return Optional.empty();
        } else if (parameterNode.kind() == SyntaxKind.REQUIRED_PARAM) {
            RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
            return Optional.of(CommonUtil.toRange(requiredParameterNode.typeName().lineRange()));
        } else if (parameterNode.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
            return Optional.of(CommonUtil.toRange(defaultableParameterNode.typeName().lineRange()));
        } else if (parameterNode.kind() == SyntaxKind.REST_PARAM) {
            RestParameterNode restParameterNode = (RestParameterNode) parameterNode;
            return Optional.of(CommonUtil.toRange(restParameterNode.typeName().lineRange()));
        } else {
            // Skip other node types
            return Optional.empty();
        }
    }
}
