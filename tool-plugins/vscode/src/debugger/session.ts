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
import { InitializedEvent, StoppedEvent, OutputEvent, TerminatedEvent, 
    ContinuedEvent, LoggingDebugSession, StackFrame, Scope
} from 'vscode-debugadapter';
import { DebugManager } from './manager';
import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';
import { spawn, ChildProcess } from 'child_process';
import { find as findPort } from 'openport';
import { lookup, kill } from 'ps-node';
import * as toml from 'toml';
import { DebugProtocol } from 'vscode-debugprotocol';
import { Thread, Frame, VariableRef, ProjectConfig, AttachRequestArguments, RunningInfo, LaunchRequestArguments } from './model';

export class BallerinaDebugSession extends LoggingDebugSession {
    
    private _dirPaths: Map<string, string> = new Map();
    private _threadIndexes: Map<any, number> = new Map()
    private _nextThreadId = 1;
    private _nextFrameId = 1;
    private _nextVariableRefId = 1;
    private _threads: Map<number, Thread> = new Map()
    private _frames: Map<number, Frame> = new Map()
    private _variableRefs: Map<number, VariableRef> = new Map()
    private _debugManager : DebugManager  = new DebugManager();
    private _sourceRoot: string | undefined;
    private _debugTarget: string | undefined;
    private _ballerinaPackage: string | undefined;
    private _projectConfig: ProjectConfig | undefined;
    private _debugServer: ChildProcess | undefined;
    private _debugPort: string | undefined;
    private _debugTests: boolean = false;
    private _noDebug: boolean | undefined;

    constructor(){
        super('ballerina-debug.txt');
    }

    initializeRequest(response: DebugProtocol.InitializeResponse, args: DebugProtocol.InitializeRequestArguments) {
        response.body = response.body || {};
        response.body.supportsConfigurationDoneRequest = true;

        this._debugManager.on('debug-hit', debugArgs => {
            const serverThreadId = debugArgs.threadId || debugArgs.workerId;

            if (!this._threadIndexes.get(serverThreadId)) {
                const thread = {
                    id: this._nextThreadId++,
                    name: serverThreadId.split('-')[0],
                    serverThreadId,
                    frameIds: [],
                };

                this._threads.set(thread.id, thread);
                this._threadIndexes.set(serverThreadId, thread.id);
            }

            // we can cast this as we are sure above exists
            const threadId = <number> this._threadIndexes.get(serverThreadId);
            const threadObj = <Thread> this._threads.get(threadId);

            // Clear other frames for this thread
            threadObj.frameIds.forEach(frameId => {
                const frame = <Frame> this._frames.get(frameId);
                frame.scopes.forEach(scope => {
                    this._variableRefs.delete(scope.variablesReference);
                });

                this._frames.delete(frameId);
            })

            threadObj.frameIds = [];

            //TODO fix any type
            debugArgs.frames.forEach((frame: any) => { 
                const {fileName, frameName, lineID, packageName: packageInfo } = frame;
                let packageNameParts = packageInfo.split(':')[0].split('/');
                const packageDirname = packageNameParts[1] || packageNameParts[0];

                const frameObj = {
                    threadId,
                    id: this._nextFrameId++,
                    scopes: new Array<Scope>(),
                    fileName: path.join(packageDirname, fileName),
                    frameName,
                    line: lineID
                };

                ['Local', 'Global'].forEach(scopeName => {
                    const scope = {
                        name: scopeName,
                        variablesReference: this._nextVariableRefId++,
                        expensive: false,
                    };

                    const variables = frame.variables.filter(
                        (variable: any) => variable.scope === scopeName);

                    frameObj.scopes.push(scope);
                    this._variableRefs.set(scope.variablesReference, {
                        threadId,
                        variables,
                    });
                });

                threadObj.frameIds.push(frameObj.id);
                this._frames.set(frameObj.id, frameObj);
            });

            this.sendEvent(new StoppedEvent('breakpoint', <number> this._threadIndexes.get(serverThreadId)));
        });

        this._debugManager.on('execution-ended', () => {
            this.sendEvent(new TerminatedEvent());
        });

        this._debugManager.on('session-error', e => {
            if (e) {
                this.sendEvent(new OutputEvent(e.message));
            }
        });

        this.sendResponse(response);
    }

    attachRequest(response: DebugProtocol.AttachResponse, args:AttachRequestArguments) : void {
        const openFile = args.script;
        let cwd : string | undefined = path.dirname(openFile);
        this.setSourceRoot(cwd);

        this._debugManager.connect(`ws://${args.host}:${args.port}/debug`, () => {
            this.sendResponse(response);
            this.sendEvent(new InitializedEvent());
        });
    }

    private _getRunningInfo(currentPath: string, root: string, ballerinaPackage: string | undefined = undefined) : RunningInfo {
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

        return this._getRunningInfo(
            path.dirname(currentPath), root, path.basename(currentPath));
    }

    setSourceRoot(sourceRoot: string) {
        this._sourceRoot = sourceRoot;
    }
    
    launchRequest(response: DebugProtocol.LaunchResponse, args: LaunchRequestArguments) {
        if (!args['ballerina.home']) {
            this.terminate("Couldn't start the debug server. Please set ballerina.home.");
            return;
        }
        this._noDebug = args.noDebug;
        const openFile = args.script;
        const scriptArguments = args.scriptArguments;
        const commandOptions = args.commandOptions;
        this._debugTests = args.debugTests;
        let cwd : string | undefined = path.dirname(openFile);
        let debugTarget = path.basename(openFile);
        this._sourceRoot = cwd;
        this._ballerinaPackage = '.';

        const { sourceRoot, ballerinaPackage } =
            this._getRunningInfo(cwd, path.parse(openFile).root);

        if (sourceRoot) {
            this.setSourceRoot(sourceRoot);
        }

        if (ballerinaPackage) {
            this._ballerinaPackage = ballerinaPackage;
            debugTarget = ballerinaPackage;
            cwd = sourceRoot ;

            try {
                const balConfigString = fs.readFileSync(path.join(<string> sourceRoot, 'Ballerina.toml'));
                this._projectConfig = toml.parse(balConfigString.toString()).project;
            } catch(e) {
                // no log file
            }
        }

        let executable = path.join(args['ballerina.home'], 'bin', 'ballerina');
        if (process.platform === 'win32') {
            executable += '.bat';
        }

        this._debugTarget = debugTarget;

        // find an open port
        findPort((err: Error, port: number) => {
            if(err) {
                this.terminate("Couldn't find an open port to start the debug server.");
                return;
            }
            this._debugPort = port.toString();

            let executableArgs: Array<string> = [this._debugTests ? "test" : "run"];
            executableArgs.push('--debug');
            executableArgs.push(<string>this._debugPort);

            if (Array.isArray(commandOptions) && commandOptions.length) {
                executableArgs = executableArgs.concat(commandOptions);
            }

            executableArgs.push(<string>this._debugTarget);

            if (Array.isArray(scriptArguments) && scriptArguments.length) {
                executableArgs = executableArgs.concat(scriptArguments);
            }

            let debugServer = this._debugServer = spawn(
                executable,
                executableArgs,
                { cwd }
            );

            debugServer.on('error', (err) => {
                this.terminate("Could not start the debug server.");
            });
            
            debugServer.stdout.on('data', (data) => {
                if (`${data}`.indexOf('Ballerina remote debugger is activated on port') > -1) {
                    this._debugManager.connect(`ws://127.0.0.1:${port}/debug`, () => {
                        this.sendResponse(response);
                        this.sendEvent(new InitializedEvent());
                    });
                }

                this.sendEvent(new OutputEvent(`${data}`));
            });

            debugServer.stderr.on('data', (data) => {
                if (`${data}`.startsWith("error:")) {
                    this.terminate(`${data}`);
                } else {
                    this.sendEvent(new OutputEvent(`${data}`));
                }
            });
        });
    }

    setBreakPointsRequest(response: DebugProtocol.SetBreakpointsResponse, args: DebugProtocol.SetBreakpointsArguments) {
        if (args.source && args.source.path && args.source.name) {
            
            let fileName = args.source.path;
            let pkg = '.';

            const { _projectConfig, _ballerinaPackage } = this;
            if (_ballerinaPackage) {
                pkg = _ballerinaPackage;
            }
            if (_projectConfig) {
                pkg = `${_projectConfig['org-name']}/${_ballerinaPackage}:${_projectConfig.version}`;
            }
            this._dirPaths.set(args.source.name, path.dirname(args.source.path));
            this._debugManager.removeAllBreakpoints(fileName);
            const bps: Array<any> = [];
            if (!this._noDebug && args.breakpoints) {
                args.breakpoints.forEach((bp, i) => {
                    this._debugManager.addBreakPoint(bp.line, fileName, pkg);
                    bps.push({id: i, line: bp.line, verified: true});
                });
            }

            response.body = {
                breakpoints: bps,
            };
            this.sendResponse(response);
        }
    }

    configurationDoneRequest(response: DebugProtocol.ConfigurationDoneResponse, args: DebugProtocol.ConfigurationDoneArguments) {
        this._debugManager.startDebug();
        this.sendResponse(response);
    }

    threadsRequest(response: DebugProtocol.ThreadsResponse) {
        const threads: any = [];
        this._threads.forEach((thread: Thread) => {
            threads.push({id: thread.id, name: thread.name})
        });
        response.body = { threads };
        this.sendResponse(response);
    }

    stackTraceRequest(response: DebugProtocol.StackTraceResponse, args: DebugProtocol.StackTraceArguments) {
        const thread = this._threads.get(args.threadId);
        if (thread) {
            const stk: Array<StackFrame> = thread.frameIds.map((frameId) => {
                const frame = <Frame> this._frames.get(frameId);
                const filePath = path.join(<string> this._sourceRoot, frame.fileName);
                return {
                    id: frameId,
                    name: frame.frameName,
                    line: frame.line,
                    column: 0,
                    source: {
                        name: frame.fileName,
                        path: filePath,
                        sourceReference: 0
                    }
                };
            });
    
            response.body = {
                stackFrames: stk,
                totalFrames: stk.length
            };
            this.sendResponse(response);
        }
    }

    scopesRequest(response: DebugProtocol.ScopesResponse, args: DebugProtocol.ScopesArguments) {
        const frame = this._frames.get(args.frameId);
        if (frame) {
            response.body = {
                scopes: frame.scopes,
            };
            this.sendResponse(response);
        }
    }

    variablesRequest(response: DebugProtocol.VariablesResponse, args: DebugProtocol.VariablesArguments) {
        const varRef = this._variableRefs.get(args.variablesReference);
        if (varRef) {
            const variables = varRef.variables;
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
    }

    continueRequest(response: DebugProtocol.ContinueResponse, args: DebugProtocol.ContinueArguments) { 
        const thread= this._threads.get(args.threadId);
        if (thread) {
            this.sendEvent(new ContinuedEvent(args.threadId, false));
            this._debugManager.resume(thread.serverThreadId);
        }
    }

    nextRequest(response: DebugProtocol.NextResponse, args: DebugProtocol.NextArguments) { 
        const thread= this._threads.get(args.threadId);
        if (thread) {
            const threadId = thread.serverThreadId;
            this._debugManager.stepOver(threadId);
            this.sendResponse(response);
        }
    }

    stepInRequest(response: DebugProtocol.StepInResponse, args: DebugProtocol.StepInArguments) {
        const thread= this._threads.get(args.threadId);
        if (thread) {
            const threadId = thread.serverThreadId;
            this._debugManager.stepIn(threadId);
            this.sendResponse(response);
        }
    }

    stepOutRequest(response: DebugProtocol.StepOutResponse, args: DebugProtocol.StepOutArguments) {
        const thread= this._threads.get(args.threadId);
        if (thread) {
            const threadId = thread.serverThreadId;
            this._debugManager.stepOut(threadId);
            this.sendResponse(response);
        }
    }

    disconnectRequest(response: DebugProtocol.DisconnectResponse, args: DebugProtocol.DisconnectArguments) {
        if (this._debugServer) {
            this._debugManager.kill();
            this._debugServer.kill();
            function callBack(err: Error, resultList = [] ) {
                resultList.forEach(( process: ChildProcess ) => {
                    kill(process.pid);
                });
            };
            lookup(
                {
                    arguments: ['org.ballerinalang.launcher.Main', this._debugTests ? 'test' : 'run',
                        '--debug', this._debugPort, this._debugTarget],
                }, 
                callBack
            );
        }
        this.sendResponse(response);
    }

    terminate(msg: string) {
        this.sendEvent(new OutputEvent(msg));
        this.sendEvent(new TerminatedEvent());
    }
}
