import { ViewState } from "./view-state";

export class EndpointViewState extends ViewState {
    public visible: boolean;

    constructor() {
        super();
        this.visible = true;
    }
}
