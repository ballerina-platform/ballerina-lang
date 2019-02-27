import { ASTNode } from "@ballerina/ast-model";
import { ViewState } from "./view-state";

export class StmntViewState extends ViewState {
    public isAction: boolean = false;
    public isReturn: boolean = false;
    public endpoint: ViewState = new ViewState();
    public expanded: boolean = false;
    public expandedSubTree: ASTNode | undefined = undefined;
    public expandedSubTreeDocUri: string = "";

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
