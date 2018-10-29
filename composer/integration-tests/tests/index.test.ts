import { createConnection, ConnectionErrorHandler, ConnectionCloseHandler, IConnection } from 'monaco-languageclient/lib/connection';
import { createMessageConnection, StreamMessageReader, StreamMessageWriter } from 'vscode-jsonrpc';
import { ChildProcess, spawn } from 'child_process';
import * as path from 'path';
import { InitializeResult } from 'vscode-languageserver-protocol';

let lsConnection: IConnection;
let langServerProcess: ChildProcess;
let lsInitResult: InitializeResult;

const LS_CLASSPATH = path.join(__dirname, '..', '..', 'target', 'lib', '*');

function startServer() : ChildProcess {
    return spawn('java', 
        [
           '-cp',
           LS_CLASSPATH,
           'org.ballerinalang.langserver.launchers.stdio.Main'
        ]
    );
}

function createLSConnection() : IConnection {
    langServerProcess = startServer();
    langServerProcess.on('error', (err) => {
        console.log('Error while starting LS', err);
    });
    const reader = new StreamMessageReader(langServerProcess.stdout);
    const writer = new StreamMessageWriter(langServerProcess.stdin);
    const messageConntection = createMessageConnection(reader, writer);
    const errorHandler: ConnectionErrorHandler = (err, msg, count) => {
        console.log('Error while starting LS', err);
    };
    const closeHandler: ConnectionCloseHandler = () => {

    };
    return createConnection(messageConntection, errorHandler, closeHandler);
}

beforeAll((done) => {
    lsConnection = createLSConnection();
    lsConnection.listen();
    lsConnection.onLogMessage((params) => {
        console.log('LS LOG: ' +  params.type + ' : ' + params.message);
    });
    lsConnection.initialize({
        workspaceFolders: null,
        rootUri: null,
        processId: process.pid,
        capabilities: {
        },
    }).then((result) => {
        if (!result.capabilities) {
            console.log('Error while sending LS init request.');
        }
        lsInitResult = result;
        done();
    }, (reason) => {
        console.log('Error while sending LS init request.');
        console.log(reason);
        done();
    })
    lsConnection.onDiagnostics((params) => {

    });
});

test('Lang-server is started properly.', () => {
    expect(lsInitResult.capabilities.experimental.astProvider).toBe(true);
});

afterAll(() => {
    lsConnection.shutdown();
    if (langServerProcess) {
        langServerProcess.kill();
    }
});