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
import { SemanticHighlightingInformation } from './model';
import { getScopeColor } from './scopeTree';
import { ExtendedLangClient } from '../core/extended-language-client';

export function activate(ballerinaExtInstance: BallerinaExtension) {
    const context = <ExtensionContext>ballerinaExtInstance.context;
    
    // let highlightingInfo: SemanticHighlightingInformation[] =
    //     [{ line: 4, token: "WzEyLCAxNCwgMF0=" }, //[0, 3, 0, 5,2, 1, 10, 5, 0]
    //     { line: 5, token: "WzEyLCAxNSwgMF0=" }]; //[16, 2, 1, 20, 3, 0, 25, 2, 1]

        const langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;

        ballerinaExtInstance.onReady().then(() => {
            langClient.onNotification('window/highlighting', (highlights: SemanticHighlightingInformation) => {
                let highlightInformation: SemanticHighlightingInformation[] = [];
                highlightInformation.push(highlights); 
                setEditorDecorations(highlightInformation);
            });
        })
        .catch((e) => {
            //window.showErrorMessage('Could not start HTTP logs feature', e.message);
        });

    let disposable = commands.registerCommand('ballerina.highlightSyntax', () => {
        
        console.log(getScopeColor("string.begin.ballerina"));

    });
    
    context.subscriptions.push(disposable);
}
