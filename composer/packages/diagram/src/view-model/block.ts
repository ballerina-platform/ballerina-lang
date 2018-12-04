import { SimplePoint } from "./simple-point";
import { ViewState } from "./view-state";

export class BlockViewState extends ViewState {
    public menuTrigger: SimplePoint = new SimplePoint();
}
