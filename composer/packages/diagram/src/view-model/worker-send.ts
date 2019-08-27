
import { StmntViewState } from "./statement";
import { WorkerViewState } from "./worker";

export class WorkerSendViewState extends StmntViewState {
    public to: WorkerViewState = new WorkerViewState();
    public isSynced: boolean = false;
}
