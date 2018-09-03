/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const { 
    DebugSession, InitializedEvent, StoppedEvent, OutputEvent, TerminatedEvent, 
    ContinuedEvent, LoggingDebugSession,
} = require('vscode-debugadapter');
const DebugManager = require('./DebugManager');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { spawn } = require('child_process');
const openport = require('openport');
const ps = require('ps-node');
const toml = require('toml');

class BallerinaDebugSession extends LoggingDebugSession {
    initializeRequest(response, args) {
        response.body = response.body || {};
        response.body.supportsConfigurationDoneRequest = true;

        this.dirPaths = {};
        this.threadIndexes = {};

        this.nextThreadId = 1;
        this.nextFrameId = 1;
        this.nextVariableRefId = 1;
        this.threads = {};
        this.frames = {};
        this.variableRefs = {};
        
        this.debugManager = new DebugManager();

        this.debugManager.on('debug-hit', debugArgs => {
            const serverThreadId = debugArgs.threadId || debugArgs.workerId;

            if (!this.threadIndexes[serverThreadId]) {
                const thread = {
                    id: this.nextThreadId++,
                    name: serverThreadId.split('-')[0],
                    serverThreadId,
                    frameIds: [],
                };

                this.threads[thread.id] = thread;
                this.threadIndexes[serverThreadId] = thread.id;
            }

            const threadId = this.threadIndexes[serverThreadId];
            const threadObj = this.threads[this.threadIndexes[serverThreadId]];

            // Clear other frames for this thread
            threadObj.frameIds.forEach(frameId => {
                const frame = this.frames[frameId];
                frame.scopes.forEach(scope => {
                    delete this.variableRefs[scope.variablesReference];
                });

                delete this.frames[frameId];
            })

            threadObj.frameIds = [];

            debugArgs.frames.forEach(frame => {
                const {fileName, frameName, lineID, packageName: packageInfo } = frame;
                let packageNameParts = packageInfo.split(':')[0].split('/');
                const packageDirname = packageNameParts[1] || packageNameParts[0];

                const frameObj = {
                    threadId,
                    id: this.nextFrameId++,
                    scopes: [],
                    fileName: path.join(packageDirname, fileName),
                    frameName,
                    line: lineID
                };

                ['Local', 'Global'].forEach(scopeName => {
                    const scope = {
                        name: scopeName,
                        variablesReference: this.nextVariableRefId++,
                        expensive: false,
                    };

                    const variables = frame.variables.filter(
                        variable => variable.scope === scopeName);

                    frameObj.scopes.push(scope);
                    this.variableRefs[scope.variablesReference] = {
                        threadId,
                        variables,
                    };
                });

                threadObj.frameIds.push(frameObj.id);
                this.frames[frameObj.id] = frameObj;
            });

            this.sendEvent(new StoppedEvent('breakpoint', this.threadIndexes[serverThreadId]));
        });

        this.debugManager.on('execution-ended', () => {
            this.sendEvent(new TerminatedEvent());
        });

        this.debugManager.on('session-error', e => {
            if (e) {
                this.sendEvent(new OutputEvent(e.message));
            }
        });

        this.sendResponse(response);
    }

    attachRequest(response, args) {
        this.debugManager.connect(`ws://${args.host}:${args.port}/debug`, () => {
            this.sendResponse(response);
            this.sendEvent(new InitializedEvent());
        });
    }

    getRunningInfo(currentPath, root, ballerinaPackage) {
        if (fs.existsSync(path.join(currentPath, '.ballerina'))) {
            if (currentPath != os.homedir()) {
                return {
                    sourceRoot: currentPath,
                    ballerinaPackage,
                }
            }
        }

        if (currentPath === root) {
            return {};
        }

        return this.getRunningInfo(
            path.dirname(currentPath), root, path.basename(currentPath));
    }

    launchRequest(response, args) {
        if (!args['ballerina.home']) {
            this.terminate("Couldn't start the debug server. Please set ballerina.home.");
            return;
        }

        const openFile = args.script;
        let cwd = path.dirname(openFile);
        let fileName = path.basename(openFile);
        this.sourceRoot = cwd;
        this.ballerinaPackage = '.';

        const { sourceRoot, ballerinaPackage } =
            this.getRunningInfo(cwd, path.parse(openFile).root);

        if (sourceRoot) {
            this.sourceRoot = sourceRoot;
        }

        if (ballerinaPackage) {
            this.ballerinaPackage = ballerinaPackage;
            fileName = ballerinaPackage;
            cwd = sourceRoot;

            try {
                const balConfigString = fs.readFileSync(path.join(sourceRoot, 'Ballerina.toml'));
                this.projectConfig = toml.parse(balConfigString).project;
            } catch(e) {
                // no log file
            }
        }

        let executable = path.join(args['ballerina.home'], 'bin', 'ballerina');
        if (process.platform === 'win32') {
            executable += '.bat';
        }

        // find an open port
        openport.find((err, port) => {
            if(err) {
                this.terminate("Couldn't find an open port to start the debug server.");
                return;
            }

            let debugServer;
            debugServer = this.debugServer = spawn(
                executable,
                ['run', '--debug', port, fileName],
                { cwd }
            );

            debugServer.on('error', (err) => {
                this.terminate("Could not start the debug server.");
            });
            
            debugServer.stdout.on('data', (data) => {
                if (`${data}`.indexOf('Ballerina remote debugger is activated on port') > -1) {
                    this.debugManager.connect(`ws://127.0.0.1:${port}/debug`, () => {
                        this.sendResponse(response);
                        this.sendEvent(new InitializedEvent());
                    });
                }

                this.sendEvent(new OutputEvent(`${data}`));
            });

            debugServer.stderr.on('data', (data) => {
                if (`${data}`.indexOf('compilation contains errors') > -1) {
                    this.terminate('Failed to compile.');
                } else {
                    this.sendEvent(new OutputEvent(`${data}`));
                }
            });
        });
    }

    setBreakPointsRequest(response, args) {
        let fileName = args.source.path;
        let pkg = '.';

        const { projectConfig, ballerinaPackage } = this;
        if (ballerinaPackage) {
            pkg = ballerinaPackage;
        }
        if (projectConfig) {
            pkg = `${projectConfig['org-name']}/${ballerinaPackage}:${projectConfig.version}`;
        }
        
        this.dirPaths[args.source.name] = path.dirname(args.source.path);
        
        this.debugManager.removeAllBreakpoints(fileName);
        const bps = [];
        args.breakpoints.forEach((bp, i) => {
            this.debugManager.addBreakPoint(bp.line, fileName, pkg);
            bps.push({id: i, line: bp.line, verified: true});
        });

        response.body = {
            breakpoints: bps,
        };
        this.sendResponse(response);
    }

    configurationDoneRequest(response) {
        this.debugManager.startDebug();
        this.sendResponse(response);
    }

    threadsRequest(response, args) {
        const threads = Object.keys(this.threads).map(key => (
            {id: this.threads[key].id, name: this.threads[key].name}
        ));
        response.body = { threads };
        this.sendResponse(response);
    }

    stackTraceRequest(response, args) {
        const thread = this.threads[args.threadId];

        const stk = thread.frameIds.map((frameId) => {
            const frame = this.frames[frameId];
            const filePath = path.join(this.sourceRoot, frame.fileName);

            return {
                id: frameId,
                name: frame.frameName,
                line: frame.line,
                source: {
                    name: frame.fileName,
                    path: filePath,
                }
            };
        });

        response.body = {
            stackFrames: stk,
            totalFrames: stk.length
        };
        this.sendResponse(response);
    }

    scopesRequest(response, args) {
        const frame = this.frames[args.frameId];
        response.body = {
            scopes: frame.scopes,
        };
        this.sendResponse(response);
    }

    variablesRequest(response, args) {
        const variables = this.variableRefs[args.variablesReference].variables;
        response.body = {
            variables: variables.map(variable => ({
                name: variable.name,
                type: "integer",
                value: variable.value,
                variablesReference: 0
            })),
        }
        this.sendResponse(response);
    }

    continueRequest(response, args) { 
        const threadId = this.threads[args.threadId].serverThreadId;
        this.sendEvent(new ContinuedEvent(threadId, false));
        this.debugManager.resume(threadId);
    }

    nextRequest(response, args) { 
        const threadId = this.threads[args.threadId].serverThreadId;
        this.debugManager.stepOver(threadId);
        this.sendResponse(response);
    }

    stepInRequest(response, args) {
        const threadId = this.threads[args.threadId].serverThreadId;
        this.debugManager.stepIn(threadId);
        this.sendResponse(response);
    }

    stepOutRequest(response, args) {
        const threadId = this.threads[args.threadId].serverThreadId;
        this.debugManager.stepOut(threadId);
        this.sendResponse(response);
    }

    disconnectRequest(response) {
        if (this.debugServer) {
            this.debugManager.kill();
            this.debugServer.kill();
            ps.lookup({
                arguments: ['org.ballerinalang.launcher.Main', 'run', this.debugServer.spawnargs[2]],
                }, (err, resultList = [] ) => {
                resultList.forEach(( process ) => {
                    ps.kill(process.pid);
                });
            });
        }
        this.sendResponse(response);
    }

    terminate(msg) {
        this.sendEvent(new OutputEvent(msg));
        this.sendEvent(new TerminatedEvent());
    }
}

DebugSession.run(BallerinaDebugSession);
