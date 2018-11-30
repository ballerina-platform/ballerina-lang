import { SimpleBBox } from "./simple-bbox";
import { ViewState } from "./view-state";

export class WorkerViewState extends ViewState {
    public lifeline: SimpleBBox = new SimpleBBox();

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
