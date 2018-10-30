import { createConnection, ConnectionErrorHandler, ConnectionCloseHandler, IConnection } from 'monaco-languageclient/lib/connection';
import { createMessageConnection, StreamMessageReader, StreamMessageWriter } from 'vscode-jsonrpc';
import { ChildProcess, spawn } from 'child_process';
import { sync as globSync } from 'glob';
import { moveSync }from 'fs-extra';

import * as path from 'path';
import { InitializeResult } from 'vscode-languageserver-protocol';

const extractPath = path.join(__dirname, '..', 'target', 
            'extracted-distributions');

export const balToolsPath = path.join(extractPath, 'ballerina-tools');

const LS_DEBUG = process.env.LS_DEBUG === "true";
const LS_CUSTOM_CLASSPATH = process.env.LS_CUSTOM_CLASSPATH;

globSync(path.join(extractPath, `ballerina-tools-*`)).forEach((folder) => {
    if (folder.includes('ballerina-tools')) {
        moveSync(folder, balToolsPath);
    }
});

function createServer() : ChildProcess {
    let cmd;
    const cwd = path.join(balToolsPath, 'lib', 'tools', 'lang-server', 'launcher');
    const args: Array<string> = [];
    if (process.platform === 'win32') {
        cmd = path.join(cwd, 'language-server-launcher.bat');
    } else {
        cmd = 'sh';
        args.push(path.join(cwd, 'language-server-launcher.sh'));
    }

    if (LS_DEBUG) {
        args.push('--debug');
    }
    if (LS_CUSTOM_CLASSPATH) {
        args.push('--classpath', LS_CUSTOM_CLASSPATH);
    }
    return spawn(cmd, args, { cwd });
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
export interface GetASTParams {
    documentIdentifier: {
        uri: string;
    };
}

export interface BallerinaAST {
    kind: string;
    topLevelNodes: any[];
}

export interface GetASTResponse {
    ast: BallerinaAST;
    parseSuccess: boolean;
}

export interface MinimalLangClient {
    lsConnection: IConnection;
    initializedResult: InitializeResult;
    kill: () => void;
    getAST: (params: GetASTParams) => Thenable<GetASTResponse>;
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
            },
            getAST: (params: GetASTParams) => {
                return lsConnection.sendRequest<GetASTResponse>("ballerinaDocument/ast", params);
            }
        }
        return langClient;
    }, (reason) => {
        console.log('Error while sending LS init request.');
        console.log(reason);
        return undefined;
    });
};