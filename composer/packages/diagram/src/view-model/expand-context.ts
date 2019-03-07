import { ASTNode, Invocation } from "@ballerina/ast-model";

export class ExpandContext {
    public expandedSubTree: ASTNode | undefined;
    public expanderX: number = 0; // TODO: Check if this is xpneeded
    public expandedSubTreeDocUri: string = ""; // The uri of the bal file where the definition is in
    public expandableNode: Invocation;

    constructor(expandableNode: Invocation) {
        this.expandableNode = expandableNode;
    }
}
