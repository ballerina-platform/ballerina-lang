import { Function as BalFunction, Invocation } from "@ballerina/ast-model";

export class ExpandContext {
    public expandedSubTree: BalFunction | undefined;
    public expandedSubTreeDocUri: string = ""; // The uri of the bal file where the definition is in
    public expandableNode: Invocation;
    public labelWidth: number = 0;

    constructor(expandableNode: Invocation) {
        this.expandableNode = expandableNode;
    }
}
