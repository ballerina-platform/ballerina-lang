import { ViewState } from "./view-state";

export class WorkerViewState extends ViewState {
    public lifeline: ViewState = new ViewState();
    public name: string = "Default";
    public initHeight: number = 0;

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
