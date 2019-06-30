import { ASTNode } from "@ballerina/ast-model";

export class HiddenBlockContext {
    public expanded: boolean = false;
    public otherHiddenNodes: ASTNode[] = [];
    public statementHeight: number = 0;
}
