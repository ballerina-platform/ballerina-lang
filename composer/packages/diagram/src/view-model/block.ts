import { HoverRectBBox } from "./hover-rect";
import { SimplePoint } from "./simple-point";
import { ViewState } from "./view-state";

export class BlockViewState extends ViewState {
    public menuTrigger: SimplePoint = new SimplePoint();
    public hoverRect: HoverRectBBox = new HoverRectBBox();
    public hiddenBlocksFound: boolean = false;
}
