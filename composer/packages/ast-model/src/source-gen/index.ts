import { ASTNode } from "../ast-interfaces";
import { Visitor } from "../base-visitor";
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
        return Object.keys(this.ws)
            .map((i) => (parseInt(i, 10)))
            .sort((a, b) => (a - b))
            .reduce((source, i) => (source + this.ws[i].ws + this.ws[i].text), "");
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
}

const sourceGenVisitor = new SourceGenVisitor();

export function genSource(node: ASTNode): string {
    sourceGenVisitor.reset();
    traversNode(node, sourceGenVisitor);
    return sourceGenVisitor.getSource();
}
