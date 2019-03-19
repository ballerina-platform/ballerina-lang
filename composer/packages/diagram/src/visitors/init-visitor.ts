import {
    Assignment, ASTKindChecker, ASTNode, ASTUtil, Block,
    ExpressionStatement, Function as BalFunction, Return, VariableDef, VisibleEndpoint, Visitor, WorkerSend
} from "@ballerina/ast-model";
import { EndpointViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ExpandContext } from "../view-model/expand-context";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";
import { WorkerSendViewState } from "../view-model/worker-send";

let visibleEndpoints: VisibleEndpoint[] = [];

function initStatement(node: ASTNode) {
    if (!node.viewState) {
        node.viewState = new StmntViewState();
    }

    const viewState = node.viewState as StmntViewState;
    if (viewState.expandContext && viewState.expandContext.expandedSubTree) {
        const expandedFunction = viewState.expandContext.expandedSubTree as BalFunction;
        expandedFunction.viewState = new FunctionViewState();
        expandedFunction.viewState.isExpandedFunction = true;
        ASTUtil.traversNode(expandedFunction, visitor);
        if (expandedFunction.VisibleEndpoints) {
            visibleEndpoints = [...visibleEndpoints, ...expandedFunction.VisibleEndpoints];
        }
    }
}

export const visitor: Visitor = {

    beginVisitASTNode(node: ASTNode) {
        if (!node.viewState) {
            node.viewState = new ViewState();
        }
    },

    beginVisitBlock(node: Block, parent: ASTNode) {
        if (!node.viewState) {
            node.viewState = new BlockViewState();
        }
        node.parent = parent;
    },

    // tslint:disable-next-line:ban-types
    beginVisitFunction(node: BalFunction) {
        if (!node.viewState) {
            node.viewState = new FunctionViewState();
        }
        if (node.body) {
            node.body.statements.forEach((statement, index) => {
                // Hide All worker nodes.
                if (ASTUtil.isWorker(statement)) {
                    if (!statement.viewState) {
                        statement.viewState = new WorkerViewState();
                    }
                    statement.viewState.hidden = true;
                    const nextStmt = node.body!.statements[index + 1];
                    if (nextStmt) {
                        if (!nextStmt.viewState) {
                            nextStmt.viewState = new ViewState();
                        }
                        nextStmt.viewState.hidden = true;
                    }
                }
            });
        }
    },

    endVisitFunction(node: BalFunction) {
        const viewState = node.viewState as FunctionViewState;
        let endpoints = visibleEndpoints;

        if (node.VisibleEndpoints) {
            endpoints = [...node.VisibleEndpoints, ...endpoints];
        }

        if (!viewState.isExpandedFunction) {
            const toAdd: VisibleEndpoint[] = [];
            const added: any = {};
            endpoints.forEach((ep) => {
                if (!added[ep.name]) {
                    toAdd.push(ep);
                    added[ep.name] = true;
                }
            });
            viewState.containingVisibleEndpoints = toAdd;
            visibleEndpoints = [];
        }
    },

    beginVisitCompilationUnit(node: ASTNode) {
        // view state will be set by the diagram component.
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        initStatement(node);
        const viewState = node.viewState as StmntViewState;
        if (ASTKindChecker.isInvocation(node.expression) && !viewState.expandContext) {
            viewState.expandContext = new ExpandContext(node.expression);
        }
    },

    endVisitVariableDef(node: VariableDef) {
        initStatement(node);
        const viewState = node.viewState as StmntViewState;
        if (node.variable.initialExpression &&
            ASTKindChecker.isInvocation(node.variable.initialExpression) &&
            !viewState.expandContext) {
                viewState.expandContext = new ExpandContext(node.variable.initialExpression);
        }
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
    },

    beginVisitWorkerSend(node: WorkerSend) {
        node.viewState = new WorkerSendViewState();
    }
};
