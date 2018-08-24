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

const { workspace, commands, window, Uri, ViewColumn } = require('vscode');
const path = require('path');
const _ = require('lodash');

const { StaticProvider } = require('./content-provider');
const { render } = require('./renderer');
const DEBOUNCE_WAIT = 500;

let previewPanel;

exports.activate = function(context, langClient) {
	workspace.onDidChangeTextDocument(_.debounce((e) => {
        if ((previewPanel && window.activeTextEditor) && (e.document === window.activeTextEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			const editor = window.activeTextEditor;
			render(editor.document.getText(), langClient)
				.then((html) => {
					previewPanel.webview.html = html;
				})
				.catch((html) => {
					previewPanel.webview.html = html;
				});
		}
	}, DEBOUNCE_WAIT));

	window.onDidChangeActiveTextEditor((e) => {
        if ((previewPanel && window.activeTextEditor) && (e.document === window.activeTextEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			const editor = window.activeTextEditor;
			render(editor.document.getText(), langClient)
				.then((html) => {
					previewPanel.webview.html = html;
				})
				.catch((html) => {
					previewPanel.webview.html = html;
				});
		}
	})

	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		const resourcePath = Uri.file(path.join(context.extensionPath, 'renderer', 'resources'));
        // Create and show a new webview
        previewPanel = window.createWebviewPanel(
            'ballerinaDiagram',
            "Ballerina Diagram",
            ViewColumn.Two,
            {
				enableScripts: true,
				localResourceRoots: [resourcePath]
			}
		);
		const editor = window.activeTextEditor;
		if(!editor) {
            return "";
        }
		render(editor.document.getText(), langClient, resourcePath.with({ scheme: 'vscode-resource' }).toString())
			.then((html) => {
				previewPanel.webview.html = html;
			})
			.catch((html) => {
				previewPanel.webview.html = html;
			});
    });
	context.subscriptions.push(diagramRenderDisposable);
}

exports.errored = function(context) {
	const provider = new StaticProvider();
	let registration = workspace.registerTextDocumentContentProvider('ballerina-static', provider);

	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		let uri = Uri.parse('ballerina-static:///pages/error.html');
		commands.executeCommand('vscode.previewHtml', uri, ViewColumn.Two, 'Ballerina Diagram');
	});

	context.subscriptions.push(diagramRenderDisposable, registration);
}