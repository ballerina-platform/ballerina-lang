/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
import { BallerinaExtension } from '../core';
import { commands, ExtensionContext } from 'vscode';
import { setEditorDecorations } from './highlighter';

export function activate(ballerinaExtInstance: BallerinaExtension) {
    const context = <ExtensionContext>ballerinaExtInstance.context;
    const highlightingInfo: { line: number, token: string }[] =
        [{ line: 0, token: "WzAsIDMsIDAsIDUsMiwgMSwgMTAsIDUsIDBd" }, //[0, 3, 0, 5,2, 1, 10, 5, 0]
        { line: 1, token: "WzE2LCAyLCAxLCAyMCwgMywgMCwgMjUsIDIsIDFd" }]; //[16, 2, 1, 20, 3, 0, 25, 2, 1]

    let disposable = commands.registerCommand('ballerina.highlightSyntax', () => {
        setEditorDecorations(highlightingInfo);
    });
    
    context.subscriptions.push(disposable);
}
