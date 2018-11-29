import { SimpleBBox } from "./simple-bbox";
import { ViewState } from "./view-state";

export class CompilationUnitViewState extends ViewState {
    public container: SimpleBBox = new SimpleBBox();
}
