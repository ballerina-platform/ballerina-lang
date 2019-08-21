import { Return } from "@ballerina/ast-model";
import { ViewState } from "./view-state";

export class WorkerViewState extends ViewState {
    public lifeline: ViewState = new ViewState();
    public name: string = "Default";
    public initHeight: number = 0;
    public returnStatements: Return[] = [];

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
