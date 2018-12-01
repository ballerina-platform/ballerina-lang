import { ASTNode, Invocation, SimpleVariableRef } from "./ast-interfaces";
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

export function isActionInvocation(node: ASTNode): string | boolean {
    let isAction = false;
    let variableName = "";
    traversNode(node, {
        beginVisitInvocation(element: Invocation) {
            if (element.actionInvocation) {
                isAction = true;
                if (element.expression && ASTKindChecker.isSimpleVariableRef(element.expression)) {
                    const simpleVariableRef = element.expression as SimpleVariableRef;
                    variableName = simpleVariableRef.variableName.value;
                }
            }
        }
    });
    if (isAction) {
        // Return identifire of the endpoint.
        return variableName;
    }
    return isAction;
}
