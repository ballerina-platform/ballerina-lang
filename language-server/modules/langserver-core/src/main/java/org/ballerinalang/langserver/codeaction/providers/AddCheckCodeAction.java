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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.codeaction.providers.changetype.TypeCastCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddCheckCodeAction extends TypeCastCodeAction {

    public static final String NAME = "Add Check";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068", "BCE2800");

    public AddCheckCodeAction() {
        super();
        this.codeActionNodeTypes = Arrays.asList(CodeActionNodeType.LOCAL_VARIABLE,
                CodeActionNodeType.ASSIGNMENT);
    }

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        //Check if there is a check expression already present.
        MatchedExpressionNodeResolver expressionResolver =
                new MatchedExpressionNodeResolver(positionDetails.matchedNode());
        Optional<ExpressionNode> expressionNode = positionDetails.matchedNode().apply(expressionResolver);
        if (expressionNode.isEmpty() || expressionNode.get().kind() == SyntaxKind.CHECK_EXPRESSION) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> foundType;
        if ("BCE2068".equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(
                    CodeActionUtil.getDiagPropertyFilterFunction(
                            DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX));
        } else if ("BCE2800".equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOR_ITERABLE_FOUND_SYMBOL_INDEX);
        } else {
            foundType = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        }
        if (foundType.isEmpty()) {
            return Collections.emptyList();
        }

        if (foundType.get().typeKind() != TypeDescKind.UNION ||
                !CodeActionUtil.hasErrorMemberType((UnionTypeSymbol) foundType.get())) {
            return Collections.emptyList();
        }

        List<TextEdit> edits = new ArrayList<>();
        edits.addAll(CodeActionUtil.getAddCheckTextEdits(
                CommonUtil.toRange(diagnostic.location().lineRange()).getStart(),
                positionDetails.matchedNode(), context));
        if (edits.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(AbstractCodeActionProvider.createQuickFixCodeAction(
                CommandConstants.ADD_CHECK_TITLE, edits, context.fileUri()));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
