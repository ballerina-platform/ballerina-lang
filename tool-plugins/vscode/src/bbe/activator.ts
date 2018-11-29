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
import { commands, window, Uri, ViewColumn, ExtensionContext, WebviewPanel, workspace } from 'vscode';
import * as path from 'path';
import { render } from './renderer';
import { ExtendedLangClient } from '../core/extended-language-client';
import { ballerinaExtInstance, BallerinaExtension } from '../core';
import { WebViewRPCHandler, WebViewMethod } from '../utils';

let examplesPanel: WebviewPanel | undefined;

function showExamples(context: ExtensionContext, langClient: ExtendedLangClient) :void {
    if (examplesPanel) {
        return;
    }
    // Create and show a new webview
    examplesPanel = window.createWebviewPanel(
        'ballerinaExamples',
        "Ballerina Examples",
        { viewColumn: ViewColumn.One, preserveFocus: false } ,
        {
            enableScripts: true,
            retainContextWhenHidden: true,
        }
    );
    const remoteMethods: WebViewMethod[] = [
        {
            methodName: "openExample",
            handler: (args: any[]): Thenable<any> => {
                const url = args[0];
                const ballerinaHome = ballerinaExtInstance.getBallerinaHome();
                if (ballerinaHome) {
                    const folderPath = path.join(ballerinaHome, 'examples', url);
                    const filePath = path.join(folderPath, `${url.replace(/-/g, '_')}.bal`);
                    workspace.openTextDocument(Uri.file(filePath)).then(doc => {
                        window.showTextDocument(doc);
                     }, (err: Error) => {
                         window.showErrorMessage(err.message);
                     }); 
                }
                return Promise.resolve();
            }
        }
    ];
    WebViewRPCHandler.create(examplesPanel.webview, langClient, remoteMethods);
    const html = render(context, langClient);
    if (examplesPanel && html) {
        examplesPanel.webview.html = html;
    }
    examplesPanel.onDidDispose(() => {
        examplesPanel = undefined;
    });
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
    let langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;
    const examplesListRenderer = commands.registerCommand('ballerina.showExamples', () => {
        ballerinaExtInstance.onReady()
        .then(() => {
            const { experimental } = langClient.initializeResult!.capabilities;
            const serverProvidesExamples = experimental && experimental.examplesProvider;

            if (!serverProvidesExamples) {
                ballerinaExtInstance.showMessageServerMissingCapability();
                return;
            }

            showExamples(context, langClient);
        })
		.catch((e) => {
			if (!ballerinaExtInstance.isValidBallerinaHome()) {
				ballerinaExtInstance.showMessageInvalidBallerinaHome();
			} else {
				ballerinaExtInstance.showPluginActivationError();
			}
		});
    });
    
    context.subscriptions.push(examplesListRenderer);
}