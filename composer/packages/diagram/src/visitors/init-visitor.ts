import { ASTNode, Function, Visitor } from "@ballerina/ast-model";
import { FunctionViewState, ViewState } from "../view-model";

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
    }

};
