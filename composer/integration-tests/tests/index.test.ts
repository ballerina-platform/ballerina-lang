import { createConnection, ConnectionErrorHandler, ConnectionCloseHandler, IConnection } from 'monaco-languageclient/lib/connection';
import { createMessageConnection, StreamMessageReader, StreamMessageWriter } from 'vscode-jsonrpc';
import { ChildProcess, spawn } from 'child_process';
import * as path from 'path';

let lsConnection: IConnection;

const LS_CLASSPATH = path.join(__dirname, '..', '..', 'target', 'lib');

function startServer() : ChildProcess {
    return spawn('java', 
        [
           '-cp',
           LS_CLASSPATH.toString(),
           'org.ballerinalang.langserver.launchers.stdio.Main'
        ]
    );
}

function createLSConnection() : IConnection {
    const serverProcess = startServer();
    const reader = new StreamMessageReader(serverProcess.stdout);
    const writer = new StreamMessageWriter(serverProcess.stdin);
    const messageConntection = createMessageConnection(reader, writer);
    const errorHandler: ConnectionErrorHandler = (err, msg, count) => {
        console.log('Error in LangServer' + msg);
    };
    const closeHandler: ConnectionCloseHandler = () => {

    };
    return createConnection(messageConntection, errorHandler, closeHandler);
}

beforeAll(() => {
    console.log("before all");
    lsConnection = createLSConnection();
    lsConnection.listen();
    lsConnection.onDiagnostics((params) => {

    });
});

test("test test", () => {
    expect(true).toBe(true);
})