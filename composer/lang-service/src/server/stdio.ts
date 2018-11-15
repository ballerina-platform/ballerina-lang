import { ChildProcess } from "child_process";
import * as treekill from "tree-kill";
import { IBallerinaLangServer } from ".";
import { spawnStdioServer } from "./server";
import { detectBallerinaHome } from "./utils";

export class StdioBallerinaLangServer implements IBallerinaLangServer {

    public lsProcess: ChildProcess | undefined;

    constructor(
        private ballerinaHome: string = detectBallerinaHome()
    ) {
    }

    public start(): void {
        this.lsProcess = spawnStdioServer(this.ballerinaHome);
    }

    public shutdown(): void {
        if (this.lsProcess) {
            treekill.default(this.lsProcess.pid);
        }
    }
}
