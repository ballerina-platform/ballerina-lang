import { BallerinaEndpoint } from "@ballerina/lang-service";
import { ASTNode, Block, Function as BalFunction, If, Service, UserDefinedType, Variable } from "../ast-interfaces";
import { Visitor } from "../base-visitor";
import { ASTKindChecker } from "../check-kind-util";
import * as defaults from "../default-nodes";
import { emitTreeModified } from "../events";
import { isWorkerFuture, traversNode } from "../model-utils";

class SourceGenVisitor implements Visitor {
    private ws: any;
    private wsArray: any[];
    constructor() {
        this.ws = {};
        this.wsArray = [];
    }

    public reset() {
        this.ws = {};
        this.wsArray = [];
    }

    public getSource(): string {
        return this.getWSKeys()
            .reduce((source, i) => (source + this.ws[i].ws + this.ws[i].text), "");
    }

    public getWS(): any[] {
        return this.wsArray.sort((a, b) => (a.i - b.i));
    }

    public getAllWS(): any[] {
        return this.wsArray;
    }

    public beginVisitASTNode(node: ASTNode) {
        if (!node.ws) {
            return;
        }

        node.ws.forEach((ws) => {
            if (!this.ws[ws.i]) {
                this.ws[ws.i] = ws;
            }
            this.wsArray.push(ws);
        });
    }

    private getWSKeys() {
        return Object.keys(this.ws)
            .map((i) => (parseInt(i, 10)))
            .sort((a, b) => (a - b));
    }
}

const sourceGenVisitor = new SourceGenVisitor();

function getStartIndex(attachingNode: ASTNode, attachPointNodes: ASTNode[], insertAt: number): number {
    let previousNode = attachPointNodes[insertAt - 1];
    if (previousNode) {
        if (ASTKindChecker.isVariable(previousNode)) {
            const previousNodeVar = previousNode as Variable;
            if (previousNodeVar.service) {
                previousNode = attachPointNodes[insertAt - 3];
            }
        }

        if (isWorkerFuture(previousNode)) {
            previousNode = attachPointNodes[insertAt - 2];
        }

        const attachPointWS = getWS(previousNode);
        return attachPointWS[attachPointWS.length - 1].i + 1;
    }

    // compilationUnits does not have braces arround them, so use 1 as starting index
    if (ASTKindChecker.isCompilationUnit(attachingNode)) {
        return 1;
    }

    if (!ASTKindChecker.isBlock(attachingNode)) {
        return 1;
    }

    const blockNode = attachingNode as Block;

    if (!blockNode.parent) {
        return 1;
    }

    let parent = blockNode.parent;

    if (blockNode.isElseBlock) {
        const ifNode = parent as If;
        if (!ifNode.elseStatement) {
            return 1;
        }

        parent = ifNode.elseStatement;
    }

    const attachingNodeWS = getWS(parent);
    const indexWS = attachingNodeWS.find((ws) => (ws.text === "{"));

    return indexWS === undefined ? 1 : indexWS.i + 1;
}

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

export function attachNodeSilently(
    newNode: ASTNode, tree: ASTNode,
    attachingNode: ASTNode, attachPoint: string, insertAt: number = 0) {

    const newNodeWS = getWS(newNode);
    const treeWS = getWS(tree);
    const attachPointNodes: ASTNode[] = (attachingNode as any)[attachPoint];

    // should be index of the first whitespace of the new node
    const startIndex = getStartIndex(attachingNode, attachPointNodes, insertAt);

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
    attachPointNodes.splice(insertAt, 0, newNode);
}

export function attachNode(
    newNode: ASTNode, tree: ASTNode,
    attachingNode: ASTNode, attachPoint: string, insertAt: number = 0) {

    attachNodeSilently(newNode, tree, attachingNode, attachPoint, insertAt);
    emitTreeModified(tree, newNode);
}

export function addIfToBlock(block: Block, ast: ASTNode, insertAt?: number) {
    const ifNode = defaults.createIfNode();
    if (insertAt === undefined) {
        insertAt = block.statements.length;
    }
    attachNode(ifNode, ast, block, "statements", insertAt);
}

export function addWhileToBlock(block: Block, ast: ASTNode, insertAt?: number) {
    const whileNode = defaults.createWhileNode();
    if (insertAt === undefined) {
        insertAt = block.statements.length;
    }
    attachNode(whileNode, ast, block, "statements", insertAt);
}

export function addForeachToBlock(block: Block, ast: ASTNode, insertAt?: number) {
    const foreachNode = defaults.createForeachNode();
    if (insertAt === undefined) {
        insertAt = block.statements.length;
    }
    attachNode(foreachNode, ast, block, "statements", insertAt);
}

export function addEndpointToBlock(block: Block, ast: ASTNode, endpointDef: BallerinaEndpoint, insertAt?: number) {
    const endpointNode = defaults.createEndpointNode();
    const { name, packageName } = endpointDef;
    // Update type to match def
    const endpointType = endpointNode.variable.typeNode as UserDefinedType;
    endpointType.typeName.value = name;
    endpointType.packageAlias.value = packageName;

    const epWS = getWS(endpointNode);
    epWS.forEach((ws) => {
        if (ws.text === "http") {
            ws.text = packageName;
            return;
        }
        if (ws.text === "Client") {
            ws.text = name;
        }
    });

    if (insertAt === undefined) {
        insertAt = block.statements.length;
    }
    attachNode(endpointNode, ast, block, "statements", insertAt);
}

export function addWorkerToBlock(block: Block, ast: ASTNode, insertAt?: number) {
    const workerNode = defaults.createWorkerNode();
    if (insertAt === undefined) {
        insertAt = block.statements.length;
    }
    attachNode(workerNode, ast, block, "statements", insertAt);
}

export function renameNode(node: BalFunction | Service, newName: string) {
    node.ws.forEach((ws) => {
        if (ws.text === node.name.value) {
            ws.text = newName;
        }
    });
    node.name.value = newName;
    emitTreeModified(node, node);
}
