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
import { BallerinaExtension } from '../core';
import { commands, ExtensionContext } from 'vscode';
import { Base64 } from 'js-base64';
import { Range } from 'vscode';
import { HighlightToken } from './highlight-token';

function decodeBase64(element: { line: number, token: string }): HighlightToken[] {
    const tokenArray: HighlightToken[] = [];
    let decodedText = Base64.atob(element.token);
    let decodedArray: number[] = JSON.parse(decodedText);

    for (let index = 0; index < decodedArray.length; index = index + 3) {
        let range: Range = new Range(element.line, decodedArray[index], element.line, decodedArray[index] + decodedArray[index + 1]);
        let scope = decodedArray[index + 2];
        tokenArray.push({ scope, range });
    }
    return tokenArray;
}

function highlightLines(highlightingInfo: { line: number, token: string }[]) {
    let highlights: HighlightToken[] = [];

    highlightingInfo.forEach(element => {
        highlights.push(...decodeBase64(element));
    });
    console.log(highlights);
}

export function activate(ballerinaExtInstance: BallerinaExtension) {

    const context = <ExtensionContext>ballerinaExtInstance.context;

    const highlightingInfo: { line: number, token: string }[] =
        [{ line: 0, token: "WzAsIDMsIDAsIDUsMiwgMCwgMTAsIDUsIDBd" },
        { line: 1, token: "WzE2LCAyLCAwLCAyMCwgMywgMCwgMjUsIDIsIDBd" }];

    let disposable = commands.registerCommand('ballerina.highlightSyntax', () => {
        highlightLines(highlightingInfo);
    });
    context.subscriptions.push(disposable);

}
