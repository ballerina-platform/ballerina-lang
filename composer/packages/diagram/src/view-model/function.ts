import { SimpleBBox } from "./simple-bbox";
import { ViewState } from "./view-state";

export class FunctionViewState extends ViewState {
    public header: SimpleBBox = new SimpleBBox();
    public body: SimpleBBox = new SimpleBBox();
    public client: SimpleBBox = new SimpleBBox();
    public defaultWorker: SimpleBBox = new SimpleBBox();

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
