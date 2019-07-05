import {  Function as BalFunction, Invocation } from "@ballerina/ast-model";
import * as _ from "lodash";

export class ExpandContext {
    public labelText: string = "";
    public expandedSubTree: BalFunction;
    public expandedSubTreeDocUri: string = ""; // The uri of the bal file where the definition is in
    public expandableNode: Invocation;
    public labelWidth: number = 0;
    public collapsed: boolean = false;

    constructor(expandableNode: Invocation, expandedSubTree: BalFunction, uri: string) {
        this.expandableNode = expandableNode;
        this.expandedSubTree = expandedSubTree;
        this.expandedSubTreeDocUri = uri;
    }
}
