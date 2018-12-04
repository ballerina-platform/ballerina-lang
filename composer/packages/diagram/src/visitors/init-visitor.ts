import {
    Assignment, ASTNode, Block,
    ExpressionStatement, Function, Return, VariableDef, VisibleEndpoint, Visitor
} from "@ballerina/ast-model";
import { EndpointViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ReturnViewState } from "../view-model/return";

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

    beginVisitBlock(node: Block) {
        if (!node.viewState) {
            node.viewState = new BlockViewState();
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
    },

    beginVisitVisibleEndpoint(node: VisibleEndpoint) {
        if (!node.viewState) {
            node.viewState = new EndpointViewState();
        }
        (node.viewState as EndpointViewState).visible = false;
    },

    beginVisitReturn(node: Return) {
        if (!node.viewState) {
            node.viewState = new ReturnViewState();
        }
    }
};
