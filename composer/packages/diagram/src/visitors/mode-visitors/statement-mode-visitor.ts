import { Assignment, ASTNode, ASTUtil, ExpressionStatement, Invocation,
    Return, VariableDef, Visitor } from "@ballerina/ast-model";
import { StmntViewState } from "../../view-model/index";

const currentState: {
    topLevelNode?: ASTNode | undefined,
    blocks: ASTNode[],
    currentBlock?: ASTNode,
    statement?: ASTNode | undefined,
} = {
    blocks: [],
};

export const visitor: Visitor = {
    beginVisitASTNode(node: ASTNode) {
        if (node.viewState) {
            // show all elements
            node.viewState.hidden = false;
            node.viewState.hiddenBlock = false;
        }
    },

    beginVisitVariableDef(node: VariableDef) {
        node.viewState.hidden = false;
        node.viewState.hiddenBlock = false;
        currentState.statement = node;
    },

    endVisitVariableDef(node: VariableDef) {
        currentState.statement = undefined;
    },

    beginVisitExpressionStatement(node: ExpressionStatement) {
        node.viewState.hidden = false;
        currentState.statement = node;
        node.viewState.hiddenBlock = false;
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        currentState.statement = undefined;
    },

    beginVisitAssignment(node: Assignment) {
        node.viewState.hidden = false;
        currentState.statement = node;
        node.viewState.hiddenBlock = false;
    },

    endVisitAssignment(node: Assignment) {
        currentState.statement = undefined;
    },

    beginVisitReturn(node: Return) {
        node.viewState.hidden = false;
        currentState.statement = node;
    },

    endVisitReturn(node: Return) {
        currentState.statement = undefined;
    },

    beginVisitInvocation(node: Invocation) {
        const currentStatement = currentState.statement;

        if (currentStatement) {
            const statementViewState: StmntViewState = currentStatement.viewState;
            if (!(statementViewState.expandContext && statementViewState.expandContext.expandedSubTree)) {
                return;
            }
            const { expandedSubTree } = statementViewState.expandContext;
            ASTUtil.traversNode(expandedSubTree, visitor);
        }
    }
};
