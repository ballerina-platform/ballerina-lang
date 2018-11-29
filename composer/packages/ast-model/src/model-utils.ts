import { ASTNode } from "./ast-interfaces";
import { Visitor } from "./base-visitor";

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
