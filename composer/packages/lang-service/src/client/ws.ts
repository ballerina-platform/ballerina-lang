// tslint:disable-next-line:no-submodule-imports
import { ConnectionCloseHandler, ConnectionErrorHandler, createConnection } from "monaco-languageclient/lib/connection";
import * as rpc from "vscode-ws-jsonrpc";
import { BallerinaLangClient } from "./client";
import { IBallerinaLangClient } from "./model";

export function createWSLangClient(
        port: number,
        errorHandler: ConnectionErrorHandler,
        closeHandler: ConnectionCloseHandler)
    : Thenable<IBallerinaLangClient> {

    const webSocket = new WebSocket(`ws://127.0.0.1:${port}`);

    return new Promise((resolve, reject) => {
        rpc.listen({
            onConnection: (connection: rpc.MessageConnection) => {
                const lsConnection = createConnection(connection, errorHandler, closeHandler);
                resolve(new BallerinaLangClient(lsConnection));
            },
            webSocket,
        });
    });
}
