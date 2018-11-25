import { ASTNode } from "../ast-interfaces";
import { Visitor } from "../base-visitor";
import { emitTreeModified } from "../events";
import { traversNode } from "../model-utils";

class SourceGenVisitor implements Visitor {
    private ws: any;
    constructor() {
        this.ws = {};
    }

    public reset() {
        this.ws = {};
    }

    public getSource(): string {
        return this.getWSKeys()
            .reduce((source, i) => (source + this.ws[i].ws + this.ws[i].text), "");
    }

    public getWS(): any[] {
        return this.getWSKeys()
            .map((i) => (this.ws[i]));
    }

    public beginVisitASTNode(node: ASTNode) {
        if (!node.ws) {
            return;
        }

        node.ws.forEach((ws) => {
            if (!this.ws[ws.i]) {
                this.ws[ws.i] = ws;
            }
        });
    }

    private getWSKeys() {
        return Object.keys(this.ws)
            .map((i) => (parseInt(i, 10)))
            .sort((a, b) => (a - b));
    }
}

const sourceGenVisitor = new SourceGenVisitor();

export function genSource(node: ASTNode): string {
    sourceGenVisitor.reset();
    traversNode(node, sourceGenVisitor);
    return sourceGenVisitor.getSource();
}

export function getWS(node: ASTNode): any[] {
    sourceGenVisitor.reset();
    traversNode(node, sourceGenVisitor);
    return sourceGenVisitor.getWS();
}

export function attachNodeSilently(newNode: ASTNode, tree: ASTNode, attachPoint: string, insertAt: number = 0) {
    const newNodeWS = getWS(newNode);
    const treeWS = getWS(tree);
    const attachPointNodes: ASTNode[] = (tree as any)[attachPoint];

    let startIndex = 1; // should be index of the first whitespace of the new node

    if (attachPointNodes[insertAt - 1]) {
        const attachPointWS = getWS(attachPointNodes[insertAt - 1]);
        startIndex = attachPointWS[attachPointWS.length - 1].i + 1;
    }

    // get the diff between the current and should be index of the first ws of new node
    const newNodeDiff = startIndex - newNodeWS[0].i;
    // update new node's  ws
    newNodeWS.forEach((ws) => {
        ws.i = ws.i + newNodeDiff;
    });

    // get the range of new nodes ws. tree should be updated to accomadate these new ws.
    const treeDiff = newNodeWS[newNodeWS.length - 1].i - startIndex + 1;
    // update rest of the tree
    treeWS.forEach((ws) => {
        if (ws.i >= startIndex) {
            ws.i = ws.i + treeDiff;
        }
    });
    attachPointNodes[insertAt] = newNode;
}

export function attachNode(newNode: ASTNode, tree: ASTNode, attachPoint: string, insertAt: number = 0) {
    attachNodeSilently(newNode, tree, attachPoint, insertAt);
    emitTreeModified(tree, newNode);
}
