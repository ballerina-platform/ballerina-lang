import { EventEmitter } from "events";
import { ASTNode } from "./ast-interfaces";

const emitter = new EventEmitter();

export function onTreeModified(callback: (tree: ASTNode, newNode: ASTNode) => void) {
    emitter.on("tree-modified", callback);
}

export function emitTreeModified(tree: ASTNode, newNode: ASTNode) {
    emitter.emit("tree-modified", tree, newNode);
}
