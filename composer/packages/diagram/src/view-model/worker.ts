import { ViewState } from "./view-state";

export class WorkerViewState extends ViewState {
    public lifeline: ViewState = new ViewState();

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
