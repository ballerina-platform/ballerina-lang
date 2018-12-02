import { SimpleBBox } from "./simple-bbox";
import { SimplePoint } from "./simple-point";
import { ViewState } from "./view-state";
import { WorkerViewState } from "./worker";

export class FunctionViewState extends ViewState {
    public header: SimpleBBox = new SimpleBBox();
    public body: SimpleBBox = new SimpleBBox();
    public client: ViewState = new ViewState();
    public defaultWorker: WorkerViewState = new WorkerViewState();
    public menuTrigger: SimplePoint = new SimplePoint();

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
