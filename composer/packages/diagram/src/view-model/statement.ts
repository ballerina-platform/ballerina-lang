import { ViewState } from "./view-state";

export class StmntViewState extends ViewState {
    public isAction: boolean = false;
    public isReturn: boolean = false;
    public endpoint: ViewState = new ViewState();

    constructor() {
        super();
        this.bBox.opaque = true;
    }
}
