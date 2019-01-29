
import { SimpleBBox } from "./simple-bbox";

export class ViewState {
    public bBox: SimpleBBox = new SimpleBBox();
    public hidden: boolean = false;
    public hiddenBlock: boolean = false;
    public synced: boolean = false;
}
