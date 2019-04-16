import { injectable } from "inversify";
import { BaseLanguageServerContribution, IConnection } from "@theia/languages/lib/node";
import { spawnStdioServer, detectBallerinaHome } from "@ballerina/lang-service";
import { createStreamConnection } from 'vscode-ws-jsonrpc/lib/server';
import { BALLERINA_LANGUAGE_ID, BALLERINA_LANGUAGE_NAME } from '../common';


@injectable()
export class BallerinaLanguageServerContribution extends BaseLanguageServerContribution {

    readonly id = BALLERINA_LANGUAGE_ID;
    readonly name = BALLERINA_LANGUAGE_NAME;

    start(clientConnection: IConnection): void {
        const serverProcess = spawnStdioServer(detectBallerinaHome());
        const serverConnection = createStreamConnection(serverProcess.stdout,
            serverProcess.stdin, () => serverProcess.kill());
        this.forward(clientConnection, serverConnection);
    }
}