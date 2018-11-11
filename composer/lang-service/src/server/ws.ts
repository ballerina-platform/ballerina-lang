import { ChildProcess } from "child_process";
import * as treekill from "tree-kill";
import { Server } from "ws";
import { IBallerinaLangServer } from "./model";
import { spawnWSServer } from "./server";
import { detectBallerinaHome } from "./utils";

export class WSBallerinaLangServer implements IBallerinaLangServer {

    private lsProcess: ChildProcess | undefined;
    private wsServer: Server | undefined;

    constructor(
        private port: number = 0,
        private ballerinaHome: string = detectBallerinaHome(),
    ) {
    }

    public start(): void {
        const servers = spawnWSServer(this.ballerinaHome, this.port);
        this.lsProcess = servers[0];
        this.wsServer = servers[1];
    }

    public shutdown(): void {
        if (this.lsProcess) {
            treekill(this.lsProcess.pid);
        }
        if (this.wsServer) {
            this.wsServer.removeAllListeners();
            this.wsServer.close();
        }
    }
}
