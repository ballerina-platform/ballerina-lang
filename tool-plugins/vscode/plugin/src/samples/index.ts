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
import * as _ from 'lodash';
import { render } from './renderer';
import { ExtendedLangClient } from '../lang-client';
import { getPluginConfig } from '../config';

let samplesPanel: WebviewPanel | undefined;

export function activate(context: ExtensionContext, langClient: ExtendedLangClient) {

	const resourcePath = Uri.file(path.join(context.extensionPath, 'resources', 'diagram'));
	const resourceRoot = resourcePath.with({ scheme: 'vscode-resource' });

	const samplesListRenderer = commands.registerCommand('ballerina.showExamples', () => {
		if (samplesPanel) {
			samplesPanel.reveal(ViewColumn.One, true);
			return;
		}
		// Create and show a new webview
        samplesPanel = window.createWebviewPanel(
            'ballerinaExamples',
            "Ballerina Examples",
            { viewColumn: ViewColumn.One, preserveFocus: true } ,
            {
				enableScripts: true,
				localResourceRoots: [resourcePath],
				retainContextWhenHidden: true,
			}
		);
		render(langClient, resourceRoot)
			.then((html) => {
				if (samplesPanel && html) {
					samplesPanel.webview.html = html;
				}
			});
		// Handle messages from the webview
        samplesPanel.webview.onDidReceiveMessage(message => {
            switch (message.command) {
                case 'openSample':
                    const url = JSON.parse(message.url);
                    const ballerinaHome = getPluginConfig().home;
                    if (ballerinaHome) {
                        const folderPath = path.join(ballerinaHome, 'docs', 'examples', url);
                        const filePath = path.join(folderPath, `${url.replace(/-/g, '_')}.bal`);
                        workspace.updateWorkspaceFolders(
                            workspace.workspaceFolders ? workspace.workspaceFolders.length : 0,
                            null,
                            { uri: Uri.file(folderPath) }
                        );
                        workspace.openTextDocument(Uri.file(filePath)).then(doc => {
                            window.showTextDocument(doc);
                         });            
                    }
                    return;
            }
		}, undefined, context.subscriptions);
		samplesPanel.onDidDispose(() => {
			samplesPanel = undefined;
		});
    });
	context.subscriptions.push(samplesListRenderer);
}


