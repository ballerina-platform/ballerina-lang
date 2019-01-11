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
import { DebugClient } from "vscode-debugadapter-testsupport";
import assert = require('assert');
import { IPartialLocation } from "vscode-debugadapter-testsupport/lib/debugClient";

export class DebugClientEx extends DebugClient {
    hitBreakpoint(launchArgs: any, location: any, expectedStopLocation?: IPartialLocation, expectedBPLocation?: IPartialLocation): Promise<any> {
        return Promise.all([
            this.waitForEvent('initialized', 20000).then(()=>{
                return this.setBreakpointsRequest({
                    lines: [location.line],
                    breakpoints: [{ line: location.line, column: location.column }],
                    source: { path: location.path, name: location.name},
                });
            }).then(()=>{
                return this.configurationDoneRequest();
            }),
            this.launch(launchArgs),
            this.waitForEvent('stopped', 20000).then(event => {
                assert.equal(event.body.reason, "breakpoint");
                return this.stackTraceRequest({
                    threadId: event.body.threadId,
                }).then( response =>{
                    const frame: any = response.body.stackFrames[0];
                    if (typeof location.path === 'string') {
                        this.assertPath(frame.source.path, location.path, 'stopped location: path mismatch');
                    }
                    if (typeof location.line === 'number') {
                        assert.equal(frame.line, location.line, 'stopped location: line mismatch');
                    }
                    if (typeof location.column === 'number') {
                        assert.equal(frame.column, location.column, 'stopped location: column mismatch');
                    }
                    return response;
                }).then(()=>{
                    this.continueRequest({
                        threadId: event.body.threadId,
                    });
                    return Promise.resolve();
                });
            })
        ]);
    }
}