import { ASTNode, Invocation, SimpleVariableRef, Variable, VariableDef } from "./ast-interfaces";
import { Visitor } from "./base-visitor";
import { ASTKindChecker } from "./check-kind-util";

const metaNodes = ["viewState", "ws", "position"];

export function traversNode(node: ASTNode, visitor: Visitor) {
    let beginVisitFn: any = (visitor as any)[`beginVisit${node.kind}`];
    if (!beginVisitFn) {
        beginVisitFn = visitor.beginVisitASTNode && visitor.beginVisitASTNode;
    }

    if (beginVisitFn) {
        beginVisitFn.bind(visitor)(node);
    }

    const keys = Object.keys(node);

    keys.forEach((key) => {
        if (metaNodes.includes(key)) {
            return;
        }

        const childNode = (node as any)[key] as any;
        if (Array.isArray(childNode)) {
            childNode.forEach((elementNode) => {
                if (!elementNode.kind) {
                    return;
                }

                traversNode(elementNode, visitor);
            });
            return;
        }

        if (!childNode.kind) {
            return;
        }

        traversNode(childNode, visitor);
    });

    let endVisitFn: any = (visitor as any)[`endVisit${node.kind}`];
    if (!endVisitFn) {
        endVisitFn = visitor.endVisitASTNode && visitor.endVisitASTNode;
    }
    if (endVisitFn) {
        endVisitFn.bind(visitor)(node);
    }
}

export function isActionInvocation(node: ASTNode): Invocation | boolean {
    let invocation;
    traversNode(node, {
        beginVisitInvocation(element: Invocation) {
            if (element.actionInvocation) {
                invocation = element;
            }
        }
    });
    if (invocation) {
        // Return identifire of the endpoint.
        return invocation;
    }
    return false;
}

export function getEndpointName(node: Invocation): string | undefined {
    if (node.expression && ASTKindChecker.isSimpleVariableRef(node.expression)) {
        const simpleVariableRef = node.expression as SimpleVariableRef;
        return simpleVariableRef.variableName.value;
    }
    return;
}

export function isWorker(node: ASTNode) {
    if (ASTKindChecker.isVariableDef(node)) {
        if (ASTKindChecker.isVariable((node as VariableDef).variable)) {
            const name: string = ((node as VariableDef).variable as Variable).name.value;
            if (/^0.*/.test(name)) {
                return true;
            }
        }
    }
    return false;
}
