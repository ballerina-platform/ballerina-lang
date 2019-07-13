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
        if (!(ASTUtil.isWorker(node) || ASTUtil.isWorkerFuture(node))) {
            node.viewState.hidden = false;
        }
        node.viewState.hiddenBlock = false;
        (node.viewState as StmntViewState).hiddenBlockContext = undefined;
        currentState.statement = node;
    },

    endVisitVariableDef(node: VariableDef) {
        currentState.statement = undefined;
    },

    beginVisitExpressionStatement(node: ExpressionStatement) {
        node.viewState.hidden = false;
        currentState.statement = node;
        node.viewState.hiddenBlock = false;
        (node.viewState as StmntViewState).hiddenBlockContext = undefined;
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        currentState.statement = undefined;
    },

    beginVisitAssignment(node: Assignment) {
        node.viewState.hidden = false;
        currentState.statement = node;
        node.viewState.hiddenBlock = false;
        (node.viewState as StmntViewState).hiddenBlockContext = undefined;
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
