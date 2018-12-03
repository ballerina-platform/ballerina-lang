import { ViewState } from "./view-state";

export class ReturnViewState extends ViewState {
    public isAction: boolean = false;
    public endpoint: ViewState = new ViewState();
    public client: ViewState = new ViewState();

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
