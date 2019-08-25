import { Assignment, ASTKindChecker, ASTNode, ASTUtil,
         ExpressionStatement, VariableDef, Visitor } from "@ballerina/ast-model";
import { StmntViewState } from "../view-model/index";

export const visitor: Visitor = {
    endVisitExpressionStatement(node: ExpressionStatement) {
        if (ASTUtil.isActionInvocation(node)) {
            return;
        }

        const viewState = node.viewState as StmntViewState;
        if (ASTKindChecker.isInvocation(node.expression)) {
            resetExpandings(node.expression, viewState);
        }
    },

    endVisitVariableDef(node: VariableDef) {
        if (ASTKindChecker.isVariable(node.variable) && node.variable.initialExpression) {
            resetExpandings(node.variable.initialExpression, node.viewState as StmntViewState);
        }
    },

    endVisitAssignment(node: Assignment) {
        resetExpandings(node.expression, node.viewState as StmntViewState);
    }
};

function resetExpandings(expression: ASTNode, viewState: StmntViewState) {
    if (viewState.expandContext) {
        viewState.expandContext.skipDepthCheck = false;
        if (viewState.expandContext.expandedSubTree) {
            ASTUtil.traversNode(viewState.expandContext.expandedSubTree, visitor);
        }
    }
}
