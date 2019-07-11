import { ASTNode } from "@ballerina/ast-model";
import { ExpandContext } from "./expand-context";
import { HiddenBlockContext } from "./hidden-block-context";
import { ViewState } from "./view-state";

export class StmntViewState extends ViewState {
    public isAction: boolean = false;
    public isReturn: boolean = false;
    public endpoint: ViewState = new ViewState();
    public expandContext: ExpandContext | undefined;
    public otherHiddenNodes: ASTNode[] = [];
    public hiddenBlockContext: HiddenBlockContext | undefined;
    public isInHiddenBlock: boolean = false;

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
