import {
    Assignment, ASTKindChecker, ASTNode, ASTUtil, Block,
    ExpressionStatement, Function as BalFunction, Return, VariableDef, VisibleEndpoint, Visitor, WorkerSend, If
} from "@ballerina/ast-model";
import { ProjectAST } from "@ballerina/lang-service";
import { EndpointViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ExpandContext } from "../view-model/expand-context";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";
import { WorkerSendViewState } from "../view-model/worker-send";

let visibleEndpoints: VisibleEndpoint[] = [];
let envEndpoints: VisibleEndpoint[] = [];
let projectAST: ProjectAST;

function initStatement(node: ASTNode) {
    if (!node.viewState) {
        node.viewState = new StmntViewState();
    }
    // undo previous expandings
    // the same statement may be expanded inside some functions but not in others
    (node.viewState as StmntViewState).expandContext = undefined;
}

// // This function processes endpoint parameters of expanded functions
// // so that actions to these parameters can be drawn to the original endpoint passed to them
// function handleEndpointParams(expandContext: ExpandContext) {
//     const invocation = expandContext.expandableNode;
//     const expandedFunction = expandContext.expandedSubTree;
//     if (!expandedFunction || !expandedFunction.VisibleEndpoints || !expandedFunction.parameters) {
//         return;
//     }

//     const params = expandedFunction.parameters;

//     expandedFunction.VisibleEndpoints.forEach((ep) => {
//         // Find of one of the visible endpoints is actually a parameter to the function
//         params.forEach((p, i) => {
//             if (ASTKindChecker.isVariable(p)) {
//                 if (p.name.value === ep.name) {
//                     // visible endpoint is a parameter
//                     const arg = invocation.argumentExpressions[i];
//                     if (ASTKindChecker.isSimpleVariableRef(arg)) {
//                         // This parameter actually refers to an endpoint with name in arg.variableName
//                         (ep.viewState as EndpointViewState).actualEpName = arg.variableName.value;
//                     }
//                 }
//             }
//         });
//     });
// }

// function handleExpanding(expression: ASTNode, viewState: StmntViewState) {
//     if (viewState.expandContext) {
//         return;
//     }

//     if (ASTKindChecker.isInvocation(expression)) {
//             viewState.expandContext = new ExpandContext(expression, projectAST);
//     } else if (ASTKindChecker.isCheckExpr(expression) &&
//         ASTKindChecker.isInvocation(expression.expression)) {
//             viewState.expandContext = new ExpandContext(expression.expression, projectAST);
//     }

//     if (!(viewState.expandContext && viewState.expandContext.expandedSubTree)) {
//         return;
//     }
//     const expandedFunction = viewState.expandContext.expandedSubTree;
//     expandedFunction.viewState = new FunctionViewState();
//     expandedFunction.viewState.isExpandedFunction = true;
//     ASTUtil.traversNode(expandedFunction, visitor);
//     handleEndpointParams(viewState.expandContext);
// }

// export function setProjectAST(ast: ProjectAST) {
//     projectAST = ast;
// }

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
        if (!node.parent) {
            return;
        }
        const parentNode = (parent as (If | BalFunction));
        if (parentNode.VisibleEndpoints) {
            envEndpoints = [...envEndpoints, ...parentNode.VisibleEndpoints];
        }
    },

    endVisitBlock(node: Block, parent: ASTNode) {
        if (!node.parent) {
            return;
        }
        const parentNode = (parent as (If | BalFunction));
        if (parentNode.VisibleEndpoints) {
            const visibleEndpoints = parentNode.VisibleEndpoints;
            envEndpoints = envEndpoints.filter((ep) => (!visibleEndpoints.includes(ep)));
        }
    },

    // tslint:disable-next-line:ban-types
    beginVisitFunction(node: BalFunction) {
        if (node.VisibleEndpoints) {
            visibleEndpoints = [...node.VisibleEndpoints, ...visibleEndpoints];
        }
        if (!node.viewState) {
            const viewState = new FunctionViewState();
            node.viewState = viewState;
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

        if (viewState.isExpandedFunction) {
            const toAdd: VisibleEndpoint[] = [];
            const added: any = {};
            visibleEndpoints.forEach((ep) => {
                if (!added[ep.name]) {
                    toAdd.push(ep);
                    added[ep.name] = true;
                }
            });
            viewState.containingVisibleEndpoints = toAdd;
        } else {
            visibleEndpoints = [];
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

        // const viewState = node.viewState as StmntViewState;
        // if (ASTKindChecker.isInvocation(node.expression) && !viewState.expandContext) {
        //     handleExpanding(node.expression, viewState);
        // }
    },

    endVisitVariableDef(node: VariableDef) {
        initStatement(node);

        // if (ASTUtil.isActionInvocation(node)) {
        //     return;
        // }

        // if (ASTKindChecker.isVariable(node.variable) && node.variable.initialExpression) {
        //     handleExpanding(node.variable.initialExpression, node.viewState as StmntViewState);
        // }
    },

    endVisitAssignment(node: Assignment) {
        initStatement(node);
        // handleExpanding(node.expression, node.viewState as StmntViewState);
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
    },

    beginVisitWorkerSend(node: WorkerSend) {
        node.viewState = new WorkerSendViewState();
    },
};
