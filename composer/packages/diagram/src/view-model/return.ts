import { ViewState } from "./view-state";
import { WorkerViewState } from "./worker";

export class ReturnViewState extends ViewState {
    public isAction: boolean = false;
    public endpoint: ViewState = new ViewState();
    public client: ViewState = new ViewState();
    public containingWokerViewState: WorkerViewState | undefined = undefined;
    public callerViewStates: {[name: string]: ViewState} = {};

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
