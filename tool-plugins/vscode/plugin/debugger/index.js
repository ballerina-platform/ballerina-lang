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
    DebugSession, InitializedEvent, StoppedEvent, OutputEvent, TerminatedEvent, LoggingDebugSession,
} = require('vscode-debugadapter');
const DebugManager = require('./DebugManager');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const openport = require('openport');

class BallerinaDebugSession extends LoggingDebugSession {
    initializeRequest(response, args) {
        response.body = response.body || {};
        response.body.supportsConfigurationDoneRequest = true;
        this.packagePaths = {};
        this.dirPaths = {};
        this.debugManager = new DebugManager();
        this.started = false;

        this.debugManager.on('debug-hit', debugArgs => {
            this.debugArgs = debugArgs;
            this.currentThread = debugArgs.threadId;
            this.sendEvent(new StoppedEvent('breakpoint', 1));
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

    launchRequest(response, args) {
        if (!args['ballerina.sdk']) {
            this.terminate("Couldn't start the debug server. Please set ballerina.sdk.");
            return;
        }

        const openFile = args.script;
        let cwd = path.dirname(openFile);
        let fileName = path.basename(openFile);
        
        const content = fs.readFileSync(openFile);
        const pkgMatch = content.toString().match('package\\s+([a-zA_Z_][\\.\\w]*);');
        if (pkgMatch && pkgMatch[1]) {
            const pkg = pkgMatch[1];
            const pkgParts = pkg.split('.');
            for(let i=0; i<pkgParts.length; i++) {
                cwd = path.dirname(cwd);
            }
            fileName = path.join(...pkgParts);
        }

        let executable = path.join(args['ballerina.sdk'], 'bin', 'ballerina');
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
                ['run', fileName, '--debug', port],
                { cwd }
            );
    
            this.startTimeout(args['debugServerTimeout']||5000);

            debugServer.on('error', (err) => {
                this.terminate("Could not start the debug server.");
            });
            
            debugServer.stdout.on('data', (data) => {
                if (`${data}`.indexOf('Ballerina remote debugger is activated on port') > -1) {
                    this.debugManager.connect(`ws://127.0.0.1:${port}/debug`, () => {
                        this.sendResponse(response);
                        this.started = true;

                        if (this.timedOut) {
                            this.debugManager.kill();
                        } else {
                            this.sendEvent(new InitializedEvent());
                        }
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

        const content = fs.readFileSync(args.source.path);
        const pkgMatch = content.toString().match('package\\s+([a-zA_Z_][\\.\\w]*);');
        if (pkgMatch && pkgMatch[1]) {
            pkg = pkgMatch[1];
            fileName = args.source.name;
            this.packagePaths[pkg] = path.dirname(args.source.path);
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
        const a = args;
        response.body = {
          threads: [ {id : 1, name: this.currentThread} ]
        };
        this.sendResponse(response);
    }

    stackTraceRequest(response, args) {
        const { packagePath } = this.debugArgs.location;

        const stk = this.debugArgs.frames.map((frame, i) => {
            let root = this.dirPaths[frame.fileName];
            if (packagePath !== '.') {
                root = this.packagePaths[packagePath];
            }

            return {
                id: i,
                name: frame.frameName,
                line: frame.lineID,
                source: {
                    name: frame.fileName,
                    path: path.join(root, frame.fileName),
                }
            };
        });

        const debugArgs = this.debugArgs;

        response.body = {
            stackFrames: stk,
            totalFrames: stk.length
        };
        this.sendResponse(response);
    }

    scopesRequest(response, args) {
        response.body = {
            scopes:[
                {
                    name: 'Local',
                    variablesReference: args.frameId + 1, // This can't be 0. As 0 indicates "don't fetch".
                    expensive: false,
                },
                {
                    name: 'Global',
                    variablesReference: args.frameId + 1001, // variableRefs larger than 1000 refer to globals
                    expensive: false,
                },
            ]
        };
        this.sendResponse(response);
    }

    variablesRequest(response, args) {
        let scope = "Local";
        let frameId = args.variablesReference - 1;
        if (frameId > 999) {
            scope = "Global";
            frameId = frameId - 1000;
        }
        const frame = this.debugArgs.frames[frameId];
        const varsInScope = frame.variables.filter(v => v.scope === scope);
        response.body = {
            variables: varsInScope.map(variable => ({
                name: variable.name,
                type: "integer",
                value: variable.value,
                variablesReference: 0
            })),
        }
        this.sendResponse(response);
    }

    continueRequest(response, args) {
        this.debugManager.resume();
        this.sendResponse(response);
    }

    nextRequest(response, args) {
        this.debugManager.stepOver();
        this.sendResponse(response);
    }

    stepInRequest(response, args) {
        this.debugManager.stepIn();
        this.sendResponse(response);
    }

    stepOutRequest(response, args) {
        this.debugManager.stepOut();
        this.sendResponse(response);
    }

    disconnectRequest(response) {
        if (this.debugServer) {
            this.debugManager.kill();
            this.debugServer.kill();
        }
        this.sendResponse(response);
    }

    // timeout waiting for the debug server
    startTimeout(timeout=5000) {
        setTimeout(() => {
            if(!this.started) {
                this.timedOut = true;
                this.terminate('Timeout. Debug server did not start');
            }
        }, timeout);
    }

    terminate(msg) {
        this.sendEvent(new OutputEvent(msg));
        this.sendEvent(new TerminatedEvent());
    }
}

DebugSession.run(BallerinaDebugSession);
