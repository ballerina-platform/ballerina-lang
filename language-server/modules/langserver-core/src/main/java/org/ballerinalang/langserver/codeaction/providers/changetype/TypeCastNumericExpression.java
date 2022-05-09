package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionContextTypeResolver;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.model.tree.OperatorKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code action for unsupported numeric operations.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class TypeCastNumericExpression extends TypeCastCodeAction {

    public static final String NAME = "Type Cast Numeric Expression";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE4026", "BCE2070");

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        Optional<OperatorKind> operatorKind = positionDetails.diagnosticProperty(CodeActionUtil
                .getDiagPropertyFilterFunction(DiagBasedPositionDetails.DIAG_PROP_OPERATOR_INDEX));
        Optional<TypeSymbol> lhsOperand = positionDetails.diagnosticProperty(CodeActionUtil
                .getDiagPropertyFilterFunction(DiagBasedPositionDetails.DIAG_PROP_LHS_OPERAND_INDEX));
        Optional<TypeSymbol> rhsOperand = positionDetails.diagnosticProperty(CodeActionUtil
                .getDiagPropertyFilterFunction(DiagBasedPositionDetails.DIAG_PROP_RHS_OPERAND_INDEX));

        //Check if there is a type cast expression already present.
        MatchedExpressionNodeResolver expressionResolver =
                new MatchedExpressionNodeResolver(positionDetails.matchedNode());
        Optional<ExpressionNode> expressionNode = expressionResolver.findExpression(positionDetails.matchedNode());
        if (operatorKind.isEmpty() || lhsOperand.isEmpty() || rhsOperand.isEmpty()
                || expressionNode.isEmpty() || expressionNode.get().kind() != SyntaxKind.BINARY_EXPRESSION) {
            return Collections.emptyList();
        }
        BinaryExpressionNode binaryExpressionNode = (BinaryExpressionNode) expressionNode.get();

        //Check if the binary expression is used for an assignment. 
        // If so, check if the type of the variable and the types of lsh and rhs operands is a 
        // match and select te operand to cast accordingly.
        CodeActionContextTypeResolver contextTypeResolver = new CodeActionContextTypeResolver(context);
        Optional<TypeSymbol> contextType = binaryExpressionNode.apply(contextTypeResolver);

        String typeName;
        Node castExpr;
        String operand;
        if (contextType.isPresent() && contextType.get().typeKind() == lhsOperand.get().typeKind()) {
            typeName = CommonUtil.getModifiedTypeName(context, lhsOperand.get());
            castExpr = binaryExpressionNode.rhsExpr();
            operand = "RHS";
        } else {
            //If the context type can't be determined or the context type is same as the rhs expr,
            // we add the type cast to the lhs expr.
            typeName = CommonUtil.getModifiedTypeName(context, rhsOperand.get());
            castExpr = binaryExpressionNode.lhsExpr();
            operand = "LHS";
        }

        if (typeName.isEmpty()) {
            return Collections.emptyList();
        }

        List<TextEdit> edits = new ArrayList<>(getTextEdits(castExpr, typeName));
        String commandTitle = String.format(CommandConstants.ADD_TYPE_CAST_TO_NUMERIC_OPERAND_TITLE, operand);
        return Collections.singletonList(createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.QuickFix));
    }

    public String getName() {
        return NAME;
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return false;
        }
        SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
        NonTerminalNode matchedNode = CommonUtil.findNode(new Range(context.cursorPosition(),
                context.cursorPosition()), syntaxTree);
        return CodeActionNodeValidator.validate(matchedNode);
    }
}
