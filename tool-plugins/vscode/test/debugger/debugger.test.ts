'use strict';
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
 *
 */

// The module 'assert' provides assertion methods from node

import assert = require('assert');
import * as path from 'path';
import * as child_process from "child_process";
// import * as http from 'http';

import { DebugClientEx } from './debugClient';

import { getBallerinaHome } from '../test-util';

// const ballerinaHome = getBallerinaHome();

suite('Ballerina Debug Adapter', () => {

    const PROJECT_ROOT = path.join(__dirname, '../../../');

    // const DEBUG_ADAPTER = Path.join(PROJECT_ROOT, 'out/src/debugger/index.js');
    const DATA_ROOT = path.join(PROJECT_ROOT, 'test/data/');

    let dc: DebugClientEx; 
    let serverProcess: any;
    const ballerinaHome = getBallerinaHome();
    const DEBUG_PORT = 4711;
    setup(function () {
        this.timeout(60000);
        const cwd = path.join(ballerinaHome, "bin");
        
        let cmd = 'sh';
        let args : string[] = [];
        if (process.platform === 'win32') {
            cmd = path.join(cwd, 'ballerina.bat');
        } else {
            cmd = 'sh';
            args.push(path.join(cwd, 'ballerina'));
        }
        args.push('start-debugger-adapter');
        args.push(DEBUG_PORT.toString());
        serverProcess = child_process.spawn(cmd, args);
        dc = new DebugClientEx("", "", 'ballerina', { cwd: PROJECT_ROOT });
        dc.defaultTimeout = 60000;

        return new Promise((resolve)=>{
            // resolve();
            serverProcess.stdout.on('data', (data:any) => {
                if (data.toString().includes('Debug server started')) {
                    resolve();
                }
            });
            serverProcess.stderr.on('data', (data:any) => {
                console.error(`${data}`);
            });
        }).then(()=>{
            return dc.start(DEBUG_PORT);
        });
    });

    teardown(function(){
        this.timeout(60000);
        dc.terminateRequest({}).then(()=>{
            if (serverProcess) {
                serverProcess.kill();
            }
        });
        return new Promise((resolve)=>{
            serverProcess.on('close', (code: any) => {
                resolve();
                console.log(`child process exited with code ${code}`);
            });
        });
    });

    suite('vscode debugger integration tests', () => {
        test('Initialize request', function() {
            return dc.initializeRequest().then((response) => {
                response.body = response.body || {};
                assert.equal(response.body.supportsConfigurationDoneRequest, true);
            });
        }).timeout(15000);

        test('launch request', () => {
            const PROGRAM = path.join(DATA_ROOT, 'hello_world.bal');
            return Promise.all([
                dc.launch({
                    script: PROGRAM,
                    "ballerina.home": ballerinaHome,
                    request: "launch",
                    name: "Ballerina Debug",
                    "debugServer": DEBUG_PORT,
                    "debuggeePort": "5010" 
                }),
                dc.waitForEvent("initialized", 10000)
            ]);

        }).timeout(10000);

        test('should stop on a breakpoint, main function', function () {
            this.skip();
            // Doesnt work on travis
            const PROGRAM = path.join(DATA_ROOT, 'hello_world.bal');

            const launchArgs = {
                script: PROGRAM,
                "ballerina.home": ballerinaHome,
                request: "launch",
                name: "Ballerina Debug",
                "debugServer": DEBUG_PORT,
                "debuggeePort": "5010"
            };
            return Promise.all([
                dc.launch(launchArgs),
                dc.setBreakpointsRequest({
                    lines: [4],
                    breakpoints: [{ line: 4 }],
                    source: { path: PROGRAM, name: "hello_world.bal"},
                }),
                dc.waitForEvent('stopped', 80000).then(async event=>{
                    assert.equal(event.body.reason, "breakpoint");
                    const response = await dc.stackTraceRequest({
                        threadId: event.body.threadId,
                    });
                    const frame: any = response.body.stackFrames[0];
                    if (typeof PROGRAM === 'string') {
                        dc.assertPath(frame.source.path, PROGRAM, 'stopped location: path mismatch');
                    }
                    return response;
                })
            ]);
        }).timeout(100000);

        // test('should stop on a breakpoint, hello world service', function() {
        //     const PROGRAM = Path.join(DATA_ROOT, 'hello_world_service.bal');

        //     const launchArgs = {
        //         script: PROGRAM,
        //         "ballerina.home": ballerinaHome,
        //         request: "launch",
        //         name: "Ballerina Debug",
        //     };

        //     dc.on('output', (res) => {
        //         if (res.body.output.indexOf("started HTTP/WS") > -1) {
        //             http.get('http://0.0.0.0:9090/hello/sayHello');
        //         }
        //     });

        //     return dc.hitBreakpoint(launchArgs, { path: PROGRAM, name: 'hello_world_service.bal', line: 12 });
        // }).timeout(15000);

        // test('should stop on a breakpoint, hello world service - package', function() {
        //     const PROGRAM = Path.join(DATA_ROOT, 'helloPackage', 'hello', 'hello_service.bal');

        //     const launchArgs = {
        //         script: PROGRAM,
        //         "ballerina.home": ballerinaHome,
        //         request: "launch",
        //         name: "Ballerina Debug",
        //     };

        //     dc.on('output', (res) => {
        //         if (res.body.output.indexOf("started HTTP/WS") > -1) {
        //             http.get('http://0.0.0.0:9090/hello/sayHello');
        //         }
        //     });
        //     return dc.hitBreakpoint(launchArgs, { path: PROGRAM, name: 'hello_service.bal', line: 13 });
        // }).timeout(15000);

        // test('step In, hello world service - package', function() {
        //     this.skip();
        //     const PROGRAM = Path.join(DATA_ROOT, 'helloPackage', 'hello', 'hello_service.bal');
        //     const launchArgs = {
        //         script: PROGRAM,
        //         "ballerina.home": ballerinaHome,
        //         request: "launch",
        //         name: "Ballerina Debug",
        //     };

        //     dc.on('output', (res) => {
        //         if (res.body.output.indexOf("started HTTP/WS") > -1) {
        //             http.get('http://0.0.0.0:9090/hello/sayHello');
        //         }
        //     });
        //     dc.hitBreakpoint(launchArgs, { path: PROGRAM, name: 'hello_service.bal', line: 13 });

        //     return dc.waitForEvent('stopped', 12000).then((event) => {
        //         const threadId: any = event.body.threadId;
        //         return dc.stepInRequest({
        //             threadId: threadId
        //         });
        //     }).then(() => {
        //         return dc.waitForEvent('stopped', 12000).then(event => {
        //             assert.equal(event.body.reason, "breakpoint");
        //             return dc.stackTraceRequest({
        //                 threadId: event.body.threadId,
        //             });
        //         });
        //     });
        // }).timeout(15000);
    });

});
