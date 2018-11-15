import { Server } from "ws";
import { IBallerinaLangServer } from "./model";
import { spawnWSServer } from "./server";
import { detectBallerinaHome } from "./utils";

export class WSBallerinaLangServer implements IBallerinaLangServer {

    private wsServer: Server | undefined;

    constructor(
        private port: number = 0,
        private ballerinaHome: string = detectBallerinaHome()
    ) {
    }

    public start(): void {
        this.wsServer = spawnWSServer(this.ballerinaHome, this.port);
    }

    public shutdown(): void {
        if (this.wsServer) {
            this.wsServer.removeAllListeners();
            this.wsServer.close();
        }
    }
}
