import {
    Assignment, ASTKindChecker, ASTNode, ASTUtil, Block, ExpressionStatement,
    Function as BalFunction, Invocation, VariableDef, VisibleEndpoint, Visitor
} from "@ballerina/ast-model";
import { ProjectAST } from "@ballerina/lang-service";
import _ from "lodash";
import { EndpointViewState, FunctionViewState, StmntViewState } from "../view-model";
import { ExpandContext } from "../view-model/expand-context";
import { visitor as initVisitor } from "./init-visitor";

let projectAST: ProjectAST;
let envEndpoints: VisibleEndpoint[] = [];
let invocationDepth = 0;
let maxInvocationDepth = 0;
let reachedInvocationDepth = 0;

// This function processes endpoint parameters of expanded functions
// so that actions to these parameters can be drawn to the original endpoint passed to them
function handleEndpointParams(expandContext: ExpandContext) {
    const invocation = expandContext.expandableNode;
    const expandedFunction = expandContext.expandedSubTree;
    if (!expandedFunction
            || (expandedFunction.body && !expandedFunction.body.VisibleEndpoints)
            || !expandedFunction.parameters) {
        return;
    }

    const params = expandedFunction.parameters;

    if (expandedFunction.body && expandedFunction.body.VisibleEndpoints) {
        expandedFunction.body.VisibleEndpoints.forEach((ep) => {
            // Find of one of the visible endpoints is actually a parameter to the function
            params.forEach((p, i) => {
                if (ASTKindChecker.isVariable(p)) {
                    if (p.name.value === ep.name) {
                        // visible endpoint is a parameter
                        const arg = invocation.argumentExpressions[i];
                        if (ASTKindChecker.isSimpleVariableRef(arg)) {
                            // This parameter actually refers to an endpoint with name in arg.variableName
                            (ep.viewState as EndpointViewState).actualEpName = arg.variableName.value;
                        }
                    }
                }
            });
        });
    }
}

function handleExpanding(expression: ASTNode, viewState: StmntViewState) {
    let invocation;
    if (ASTKindChecker.isInvocation(expression)) {
            invocation = expression;
    } else if (ASTKindChecker.isCheckExpr(expression) &&
        ASTKindChecker.isInvocation(expression.expression)) {
            invocation = expression.expression;
    }
    if (!invocation) {
        return;
    }

    const expandedInfo = getExpandedSubTree(invocation);
    if (!expandedInfo) {
        return;
    }

    const expandedFunctionOriginalNode = expandedInfo.node;

    if (!expandedFunctionOriginalNode.viewState) {
        expandedFunctionOriginalNode.viewState = new FunctionViewState();
    }

    if ((expandedFunctionOriginalNode.viewState as FunctionViewState).isViewedExpanded) {
        // This function is already expanded. This means this is a recursive call. Stop expanding
        return;
    }

    (expandedFunctionOriginalNode.viewState as FunctionViewState).isViewedExpanded = true;
    if (!viewState.expandContext) {
        const expandedFunction = _.cloneDeep(expandedFunctionOriginalNode);
        expandedFunction.viewState.isExpandedFunction = true;
        (expandedFunction.viewState as FunctionViewState).soroundingVisibleEndpoints = [...envEndpoints];
        viewState.expandContext = new ExpandContext(invocation, expandedFunction, expandedInfo.uri);
    }
    ASTUtil.traversNode(viewState.expandContext.expandedSubTree, initVisitor);

    invocationDepth += 1;
    if (invocationDepth > reachedInvocationDepth) {
        reachedInvocationDepth = invocationDepth;
    }

    ASTUtil.traversNode(viewState.expandContext.expandedSubTree, visitor);
    invocationDepth -= 1;

    (expandedFunctionOriginalNode.viewState as FunctionViewState).isViewedExpanded = false;

    if (!viewState.expandContext.skipDepthCheck) {
        viewState.expandContext.collapsed = !((invocationDepth < maxInvocationDepth) || maxInvocationDepth === -1);
    }
    handleEndpointParams(viewState.expandContext);
}

function getExpandedSubTree(invocation: Invocation): {node: BalFunction, uri: string} | undefined {
    const definition = (invocation as any).definition;
    if (!definition) {
        return;
    }

    const defLink: string[][] = definition.slice().reverse();

    if (defLink[0][0] !== "builtin" && defLink[0][0] !== "lang.annotations") {
        return;
    }

    const isSingleBalFile = (defLink[1][0] === ".");
    const module = isSingleBalFile ? projectAST[Object.keys(projectAST)[0]] : projectAST[defLink[1][0]];

    if (!module) {
        return;
    }

    let funcNode: BalFunction | undefined;
    let uri = "";

    Object.keys(module.compilationUnits).forEach((cUnitName) => {
        const cUnit = module.compilationUnits[cUnitName];
        cUnit.ast.topLevelNodes.forEach((n) => {
            const node = n as ASTNode;

            if (ASTKindChecker.isFunction(node)) {
                if (node.name.value === defLink[2][0]) {
                    uri = cUnit.uri;
                    funcNode = node;
                }
                return;
            }

            if (ASTKindChecker.isTypeDefinition(node) && (defLink[2][1] === "OBJECT")) {
                if (node.name.value !== defLink[2][0]) {
                    return;
                }

                if ((ASTKindChecker.isObjectType(node.typeNode) && defLink[3][1] === "FUNCTION")) {
                    uri = cUnit.uri;
                    funcNode = node.typeNode.functions.find((fnode) => (
                        `${defLink[2][0]}.${fnode.name.value}` === defLink[3][0]));
                }
            }
        });
    });

    if (!funcNode) {
        return;
    }

    return {
        node: funcNode,
        uri,
    };
}

export function setProjectAST(ast: ProjectAST) {
    projectAST = ast;
    invocationDepth = 0;
    reachedInvocationDepth = 0;
}

export function setMaxInvocationDepth(depth: number) {
    maxInvocationDepth = depth;
}

export function getReachedInvocationDepth() {
    return reachedInvocationDepth;
}

export const visitor: Visitor = {
    beginVisitBlock(node: Block) {
        if (!node.parent) {
            return;
        }
        if (node.VisibleEndpoints) {
            envEndpoints = [...envEndpoints, ...node.VisibleEndpoints];
        }
    },

    endVisitBlock(node: Block, parent: ASTNode) {
        if (!node.parent) {
            return;
        }
        if (node.VisibleEndpoints) {
            const visibleEndpoints = node.VisibleEndpoints;
            envEndpoints = envEndpoints.filter((ep) => (!visibleEndpoints.includes(ep)));
        }
    },

    beginVisitFunction(node: BalFunction) {
        (node.viewState as FunctionViewState).isViewedExpanded = true;
    },

    endVisitFunction(node: BalFunction) {
        (node.viewState as FunctionViewState).isViewedExpanded = false;
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        if (ASTUtil.isActionInvocation(node)) {
            return;
        }

        const viewState = node.viewState as StmntViewState;
        handleExpanding(node.expression, viewState);
    },

    endVisitVariableDef(node: VariableDef) {
        if (ASTKindChecker.isVariable(node.variable) && node.variable.initialExpression) {
            handleExpanding(node.variable.initialExpression, node.viewState as StmntViewState);
        }
    },

    endVisitAssignment(node: Assignment) {
        handleExpanding(node.expression, node.viewState as StmntViewState);
    }
};
