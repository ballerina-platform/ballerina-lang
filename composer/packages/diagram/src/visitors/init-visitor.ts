import { Assignment, ASTNode, ExpressionStatement, Function, VariableDef, Visitor } from "@ballerina/ast-model";
import { FunctionViewState, StmntViewState, ViewState } from "../view-model";

function initStatement(node: ASTNode) {
    if (!node.viewState) {
        node.viewState = new StmntViewState();
    }
}

export const visitor: Visitor = {

    beginVisitASTNode(node: ASTNode) {
        if (!node.viewState) {
            node.viewState = new ViewState();
        }
    },

    // tslint:disable-next-line:ban-types
    beginVisitFunction(node: Function) {
        if (!node.viewState) {
            node.viewState = new FunctionViewState();
        }
    },

    beginVisitCompilationUnit(node: ASTNode) {
        // view state will be set by the diagram component.
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        initStatement(node);
    },

    endVisitVariableDef(node: VariableDef) {
        initStatement(node);
    },

    endVisitAssignment(node: Assignment) {
        initStatement(node);
    }
};
