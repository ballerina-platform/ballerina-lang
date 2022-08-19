/*
 *  Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ConvertToQueryExpressionCodeAction implements RangeBasedCodeActionProvider {

    @Override
    public String getName() {
        return "CONVERT_TO_QUERY";
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode());
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedNode = posDetails.matchedCodeActionNode();
        if (matchedNode.kind() != SyntaxKind.LOCAL_VAR_DECL || context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }

        VariableDeclarationNode node = (VariableDeclarationNode) matchedNode;
        SemanticModel semanticModel = context.currentSemanticModel().get();
        Optional<Symbol> rhsSymbol = semanticModel.symbol(node.initializer().get());
        Optional<Symbol> lhsSymbol = semanticModel.symbol(node.typedBindingPattern());
        if (rhsSymbol.isEmpty() || rhsSymbol.get().kind() != SymbolKind.VARIABLE) {
            return Collections.emptyList();
        }
        // TODO lhs can be a record field symbol, etc as well
        if (lhsSymbol.isEmpty() || lhsSymbol.get().kind() != SymbolKind.VARIABLE) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> rhsType = SymbolUtil.getTypeDescriptor(rhsSymbol.get());
        Optional<TypeSymbol> lhsType = SymbolUtil.getTypeDescriptor(lhsSymbol.get());

        if (rhsType.isEmpty()
                || lhsType.isEmpty()
                || rhsType.get().typeKind() != TypeDescKind.ARRAY
                || lhsType.get().typeKind() != TypeDescKind.ARRAY) {
            return Collections.emptyList();
        }

        // Now we know both lhs and rhs are arrays.
        // Next we have to check if the member types are assignable
        TypeSymbol lhsMemberType = ((ArrayTypeSymbol) lhsType.get()).memberTypeDescriptor();
        TypeSymbol rhsMemberType = ((ArrayTypeSymbol) rhsType.get()).memberTypeDescriptor();

        // If rhs member type is a subtype, then solution is straight forward
        if (rhsMemberType.subtypeOf(lhsMemberType)) {
            // lhs = from var item in lhs select item;
            String query = String.format("from %s item in %s select item", rhsMemberType.signature(), rhsSymbol.get().getName().get());
            List<TextEdit> edits = new ArrayList<>();
            Range range = PositionUtil.toRange(node.initializer().get().lineRange());
            edits.add(new TextEdit(range, query));
            CodeAction codeAction = CodeActionUtil.createCodeAction("Convert to query expression",
                    edits, context.fileUri(), CodeActionKind.QuickFix);
            return List.of(codeAction);
        }

        return Collections.emptyList();
    }

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(
                SyntaxKind.LOCAL_VAR_DECL
        );
    }
}
