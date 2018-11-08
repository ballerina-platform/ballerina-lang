import { ChildProcess, execSync, spawn } from 'child_process';
import * as fs from 'fs';
// tslint:disable-next-line:no-submodule-imports
import { ConnectionCloseHandler, ConnectionErrorHandler, createConnection, IConnection } from 'monaco-languageclient/lib/connection';
import { createMessageConnection, StreamMessageReader, StreamMessageWriter } from 'vscode-jsonrpc';

import * as path from 'path';
import { BallerinaLangClient } from './client';

const LS_DEBUG = process.env.LS_DEBUG === "true";
const LS_CUSTOM_CLASSPATH = process.env.LS_CUSTOM_CLASSPATH;

function createServer(ballerinaHome: string) : ChildProcess {
    let cmd;
    const cwd = path.join(ballerinaHome, 'lib', 'tools', 'lang-server', 'launcher');
    const args: string[] = [];
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

function createLSConnection(ballerinaHome: string) : LangServerProcessConnection {
    const childProcess = createServer(ballerinaHome);
    childProcess.on('error', (err) => {
        // log('Error while starting LS', err);
    });
    const reader = new StreamMessageReader(childProcess.stdout);
    const writer = new StreamMessageWriter(childProcess.stdin);
    const messageConntection = createMessageConnection(reader, writer);
    const errorHandler: ConnectionErrorHandler = (err, msg, count) => {
       // log('Error while starting LS', err);
    };
    const closeHandler: ConnectionCloseHandler = () => {
       // log('LS Connection closed');
    };
    const lsConnection = createConnection(messageConntection, errorHandler, closeHandler);
    return {
        childProcess,
        lsConnection
    }
}

interface LangServerProcessConnection {
    childProcess: ChildProcess;
    lsConnection: IConnection;
}


export function detectBallerinaHome(): string {
    // try to ditect the environment.
    const platform: string = process.platform;
    let balHome = '';
    switch (platform) {
        case 'win32': // Windows
            if (process.env.BALLERINA_HOME) {
                return process.env.BALLERINA_HOME;
            }
            try {
                balHome = execSync('where ballerina').toString().trim();
            } catch (error) {
                return balHome;
            }
            if (path) {
                balHome = balHome.replace(/bin\\ballerina.bat$/, '');
            }
            break;
        case 'darwin': // Mac OS
        case 'linux': // Linux
            // lets see where the ballerina command is.
            try {
                const output = execSync('which ballerina');
                balHome = fs.realpathSync(output.toString().trim());
                // remove ballerina bin from path
                if (path) {
                    balHome = balHome.replace(/bin\/ballerina$/, '');
                }
                break;
            } catch {
                return balHome;
            }
    }

    // If we cannot find ballerina home return empty.
    return balHome;
}

export function startBallerinaLangServer(ballerinaHome: string): Thenable<BallerinaLangClient | undefined> {
    const { childProcess, lsConnection } = createLSConnection(ballerinaHome);
    lsConnection.listen();
    lsConnection.onLogMessage((params) => {
        // log('LS LOG: ' +  params.type + ' : ' + params.message);
    });
    return lsConnection.initialize({

        capabilities: {
        },
        processId: process.pid,
        rootUri: null,
        workspaceFolders: null,
    }).then((initializedResult) => {
        if (!initializedResult.capabilities) {
            // log('Error while sending LS init request.');
        }
        return new BallerinaLangClient(childProcess, lsConnection, initializedResult);
    }, (reason) => {
        // log('Error while sending LS init request.');
        // log(reason);
        return undefined;
    });
};