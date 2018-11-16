import { Visitor } from "./base-visitor";
import { ASTNode } from "./models";

const metaNodes = ["viewState", "ws", "position"];

export function traversNode(node: ASTNode, visitor: Visitor) {
    const beginVisitFn: any = (visitor as any)[`beginVisit${node.kind}`];
    if (beginVisitFn) {
        beginVisitFn(node);
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

    const endVisitFn: any = (visitor as any)[`endVisit${node.kind}`];
    if (endVisitFn) {
        endVisitFn(node);
    }
}
