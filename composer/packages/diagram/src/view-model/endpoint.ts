import { ViewState } from "./view-state";

export class EndpointViewState extends ViewState {
    public visible: boolean = false;
    public usedAsClient: boolean = false;

    constructor() {
        super();
        this.visible = true;
    }
}
