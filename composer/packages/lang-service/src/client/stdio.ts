import { ChildProcess } from "child_process";
// tslint:disable:no-submodule-imports
import { ConnectionCloseHandler, ConnectionErrorHandler,
    createConnection } from "monaco-languageclient/lib/connection";
import { StreamMessageReader, StreamMessageWriter } from "vscode-languageserver-protocol";
import { createMessageConnection } from "vscode-ws-jsonrpc";
import { BallerinaLangClient } from "./client";
import { IBallerinaLangClient } from "./model";

export function createStdioLangClient(cp: ChildProcess,
                                      errorHandler: ConnectionErrorHandler,
                                      closeHandler: ConnectionCloseHandler):
        Thenable<IBallerinaLangClient> {
    const reader = new StreamMessageReader(cp.stdout);
    const writer = new StreamMessageWriter(cp.stdin);
    const messageConntection = createMessageConnection(reader, writer);
    const lsConnection = createConnection(messageConntection, errorHandler, closeHandler);
    return Promise.resolve(new BallerinaLangClient(lsConnection));
}
