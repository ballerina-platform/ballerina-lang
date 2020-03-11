import {
    Assignment, ASTKindChecker, ASTNode, ASTUtil, Block, BlockFunctionBody,
    ExpressionStatement, Function as BalFunction, Return, VariableDef, VisibleEndpoint, Visitor, WorkerSend
} from "@ballerina/ast-model";
import { EndpointViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";
import { WorkerSendViewState } from "../view-model/worker-send";

let visibleEPsInCurrentFunc: VisibleEndpoint[] = [];
let envEndpoints: VisibleEndpoint[] = [];
let currentWorker: VariableDef | undefined;

function initStatement(node: ASTNode) {
    if (!node.viewState) {
        node.viewState = new StmntViewState();
    }
}

function beginVisitBlock(node: Block, parent: ASTNode) {
    if (!node.viewState) {
        node.viewState = new BlockViewState();
    }
    node.parent = parent;
    if (!node.parent) {
        return;
    }
    if (node.VisibleEndpoints) {
        envEndpoints = [...envEndpoints, ...node.VisibleEndpoints];
    }
}

function endVisitBlock(node: Block, parent: ASTNode) {
    if (!node.parent) {
        return;
    }
    if (node.VisibleEndpoints) {
        const parentsVisibleEndpoints = node.VisibleEndpoints;
        envEndpoints = envEndpoints.filter((ep) => (!parentsVisibleEndpoints.includes(ep)));
    }
}

export const visitor: Visitor = {

    beginVisitASTNode(node: ASTNode) {
        if (!node.viewState) {
            node.viewState = new ViewState();
        }
    },

    beginVisitBlockFunctionBody(node: BlockFunctionBody, parent: ASTNode) {
        beginVisitBlock(node, parent);
    },

    beginVisitBlock(node: Block, parent: ASTNode) {
        beginVisitBlock(node, parent);
    },

    endVisitBlockFunctionBody(node: BlockFunctionBody, parent: ASTNode) {
        endVisitBlock(node, parent);
    },

    endVisitBlock(node: Block, parent: ASTNode) {
        endVisitBlock(node, parent);
    },

    beginVisitFunction(node: BalFunction) {
        if (!node.viewState) {
            const viewState = new FunctionViewState();
            node.viewState = viewState;
        }
        if (node.body && ASTKindChecker.isBlockFunctionBody(node.body)) {
            const blockBody = node.body as BlockFunctionBody;
            if (blockBody.VisibleEndpoints) {
                visibleEPsInCurrentFunc = [...blockBody.VisibleEndpoints, ...visibleEPsInCurrentFunc];
            }
            blockBody.statements.forEach((statement, index) => {
                // Hide All worker nodes.
                if (ASTUtil.isWorker(statement)) {
                    if (!statement.viewState) {
                        statement.viewState = new WorkerViewState();
                    }
                    statement.viewState.hidden = true;
                    const nextStmt = blockBody.statements[index + 1];
                    if (nextStmt) {
                        if (!nextStmt.viewState) {
                            nextStmt.viewState = new ViewState();
                        }
                        nextStmt.viewState.hidden = true;
                    }
                }
            });
            // fix go to source position of caller EP
            if (node.resource && blockBody.VisibleEndpoints) {
                const callerEP = blockBody.VisibleEndpoints.find((vEP) => vEP.caller);
                if (callerEP) {
                    // update position to match caller definition (which is the first param)
                    callerEP.position = node.parameters[0].position;
                }
            }
        }
    },

    endVisitFunction(node: BalFunction) {
        const viewState = node.viewState as FunctionViewState;

        if (viewState.isExpandedFunction) {
            const toAdd: VisibleEndpoint[] = [];
            const added: any = {};
            visibleEPsInCurrentFunc.forEach((ep) => {
                if (!added[ep.name]) {
                    toAdd.push(ep);
                    added[ep.name] = true;
                }
            });
            viewState.containingVisibleEndpoints = toAdd;
        } else {
            visibleEPsInCurrentFunc = [];
        }
    },

    beginVisitCompilationUnit(node: ASTNode) {
        // view state will be set by the diagram component.
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        initStatement(node);

        if (ASTUtil.isActionInvocation(node)) {
            return;
        }
    },

    beginVisitVariableDef(node: VariableDef) {
        if (ASTUtil.isWorker(node)) {
            if (!node.viewState) {
                node.viewState = new WorkerViewState();
            }
            currentWorker = node;
            (currentWorker.viewState as WorkerViewState).returnStatements = [];
        }
    },

    endVisitVariableDef(node: VariableDef) {
        initStatement(node);
        if (ASTUtil.isWorker(node)) {
            currentWorker = undefined;
        }
    },

    endVisitAssignment(node: Assignment) {
        initStatement(node);
    },

    beginVisitVisibleEndpoint(node: VisibleEndpoint) {
        if (!node.viewState) {
            node.viewState = new EndpointViewState();
        }
        // show locally defined endpoints by default
        (node.viewState as EndpointViewState).visible = node.isLocal;
    },

    beginVisitReturn(node: Return) {
        if (!node.viewState) {
            node.viewState = new ReturnViewState();
        }
        if (currentWorker) {
            const viewState = (node.viewState as ReturnViewState);
            viewState.containingWokerViewState = currentWorker.viewState;
            (currentWorker.viewState as WorkerViewState).returnStatements.push(node);
        }
    },

    beginVisitWorkerSend(node: WorkerSend) {
        node.viewState = new WorkerSendViewState();
    },
};
