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
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.codeaction.providers.changetype.TypeCastCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
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
    private static final String DIAGNOSTIC_CODE_3998 = "BCE3998";
    private static final String DIAGNOSTIC_CODE_2068 = "BCE2068";
    private static final String DIAGNOSTIC_CODE_2800 = "BCE2800";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2652", "BCE2066",
            DIAGNOSTIC_CODE_2068, DIAGNOSTIC_CODE_2800, DIAGNOSTIC_CODE_3998);

    public AddCheckCodeAction() {
        super();
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        //Check if there is a check expression already present.
        MatchedExpressionNodeResolver expressionResolver =
                new MatchedExpressionNodeResolver(positionDetails.matchedNode());
        Optional<ExpressionNode> expressionNode = expressionResolver.findExpression(positionDetails.matchedNode());
        if (expressionNode.isEmpty() || expressionNode.get().kind() == SyntaxKind.CHECK_EXPRESSION) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> foundType;
        if (DIAGNOSTIC_CODE_2068.equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(
                    CodeActionUtil.getDiagPropertyFilterFunction(
                            DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX));
        } else if (DIAGNOSTIC_CODE_2800.equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOR_ITERABLE_FOUND_SYMBOL_INDEX);
        } else if (DIAGNOSTIC_CODE_3998.equals(diagnostic.diagnosticInfo().code())) {

            foundType = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.typeOf(expressionNode.get()));
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

        Position pos = PositionUtil.toRange(diagnostic.location().lineRange()).getStart();
        // The following code may sound odd. But as per the diagnostic, when the error is in the expr of a braced 
        // expression, the diagnostic location points to the braced expression itself. To overcome that, here we
        // treat braced expressions specially and add 'check' within the parentheses.
        if (expressionNode.get().kind() == SyntaxKind.BRACED_EXPRESSION) {
            BracedExpressionNode bracedExpressionNode = (BracedExpressionNode) expressionNode.get();
            pos = PositionUtil.toRange(bracedExpressionNode.expression().location().lineRange()).getStart();
        } else if (DIAGNOSTIC_CODE_3998.equals(diagnostic.diagnosticInfo().code())) {
            // In the case of "BCE3998", we have to consider the position as the position of the initializer 
            // because the diagnostic range is provided for the variable declaration statement instead of the 
            // initializer expression
            pos = PositionUtil.toRange(expressionNode.get().location().lineRange()).getStart();
        }

        List<TextEdit> edits = new ArrayList<>(CodeActionUtil.getAddCheckTextEdits(
                pos, positionDetails.matchedNode(), context));
        if (edits.isEmpty()) {
            return Collections.emptyList();
        }

        // The following code is used to provide code actions (but not as a quick fix, since it does not fix the code) 
        // for the scenario where wait-future-expr is not a future
        if (expressionNode.get().kind() == SyntaxKind.WAIT_ACTION && context.currentSemanticModel().isPresent()) {
            WaitActionNode waitActionNode = (WaitActionNode) expressionNode.get();
            Optional<TypeSymbol> tSymbol = context.currentSemanticModel().get().typeOf(waitActionNode.waitFutureExpr());
            if (tSymbol.map(CommonUtil::getRawType).filter(t -> t.typeKind() != TypeDescKind.FUTURE).isPresent()) {
                return Collections.singletonList(CodeActionUtil.createCodeAction(
                        CommandConstants.ADD_CHECK_TITLE, edits, context.fileUri()));
            }
        }
        return Collections.singletonList(CodeActionUtil.createCodeAction(
                CommandConstants.ADD_CHECK_TITLE, edits, context.fileUri(), CodeActionKind.QuickFix));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
