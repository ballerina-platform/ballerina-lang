import { createConnection, ConnectionErrorHandler, ConnectionCloseHandler, IConnection } from 'monaco-languageclient/lib/connection';
import { createMessageConnection, StreamMessageReader, StreamMessageWriter } from 'vscode-jsonrpc';
import { ChildProcess, spawn } from 'child_process';
import * as path from 'path';
import { InitializeResult } from 'vscode-languageserver-protocol';

const LS_CLASSPATH = path.join(__dirname, '..', '..', 'target', 'lib', '*');

function createServer() : ChildProcess {
    return spawn('java', 
        [
           '-cp',
           LS_CLASSPATH,
           'org.ballerinalang.langserver.launchers.stdio.Main'
        ]
    );
}

function createLSConnection() : LangServerProcessConnection {
    const childProcess = createServer();
    childProcess.on('error', (err) => {
        console.log('Error while starting LS', err);
    });
    const reader = new StreamMessageReader(childProcess.stdout);
    const writer = new StreamMessageWriter(childProcess.stdin);
    const messageConntection = createMessageConnection(reader, writer);
    const errorHandler: ConnectionErrorHandler = (err, msg, count) => {
        console.log('Error while starting LS', err);
    };
    const closeHandler: ConnectionCloseHandler = () => {
        console.log('LS Connection closed');
    };
    const lsConnection = createConnection(messageConntection, errorHandler, closeHandler);
    return {
        childProcess,
        lsConnection
    }
}

export interface LangServerProcessConnection {
    childProcess: ChildProcess;
    lsConnection: IConnection;
}

export interface MinimalLangClient {
    lsConnection: IConnection;
    initializedResult: InitializeResult;
    kill: () => void;
}

export function startBallerinaLangServer(): Thenable<MinimalLangClient | undefined> {
    const { childProcess, lsConnection } = createLSConnection();
    lsConnection.listen();
    lsConnection.onLogMessage((params) => {
        console.log('LS LOG: ' +  params.type + ' : ' + params.message);
    });
    return lsConnection.initialize({
        workspaceFolders: null,
        rootUri: null,
        processId: process.pid,
        capabilities: {
        },
    }).then((initializedResult) => {
        if (!initializedResult.capabilities) {
            console.log('Error while sending LS init request.');
        }
        const langClient: MinimalLangClient = {
            lsConnection,
            initializedResult,
            kill: () => {
                lsConnection.shutdown();
                childProcess.kill();
            }
        }
        return langClient;
    }, (reason) => {
        console.log('Error while sending LS init request.');
        console.log(reason);
        return undefined;
    });
};