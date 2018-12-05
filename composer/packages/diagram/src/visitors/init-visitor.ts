import {
    Assignment, ASTNode, ASTUtil, Block,
    ExpressionStatement, Function, Return, VariableDef, VisibleEndpoint, Visitor
} from "@ballerina/ast-model";
import { EndpointViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";

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
            if (node.body) {
                node.body.statements.forEach((statement, index) => {
                    // Hide All worker nodes.
                    if (ASTUtil.isWorker(statement)) {
                        if (!statement.viewState) {
                            statement.viewState = new WorkerViewState();
                        }
                        statement.viewState.hidden = true;
                        if (!node.body!.statements[index + 1].viewState) {
                            node.body!.statements[index + 1].viewState = new ViewState();
                        }
                        node.body!.statements[index + 1].viewState.hidden = true;
                    }
                });
            }
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
