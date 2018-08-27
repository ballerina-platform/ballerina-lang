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

const { workspace, commands, window, Uri, ViewColumn, Range } = require('vscode');
const path = require('path');
const _ = require('lodash');

const { StaticProvider } = require('./content-provider');
const { render } = require('./renderer');
const DEBOUNCE_WAIT = 500;
const { parseContent } = require('./utils');

let previewPanel;
let activeEditor;
let preventDiagramUpdate = false;

exports.activate = function(context, langClient) {
	const resourcePath = Uri.file(path.join(context.extensionPath, 'renderer', 'resources'));
	const resourceRoot = resourcePath.with({ scheme: 'vscode-resource' }).toString();

	workspace.onDidChangeTextDocument(_.debounce((e) => {
        if ((previewPanel && activeEditor) && (e.document === activeEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			const content = activeEditor.document.getText();
			if (preventDiagramUpdate) {
				return;
			}
			parseContent(langClient, content)
				.then((body) => {
					let stale = true;
					let json;
					if (body.model) {
						stale = false;
						json = body.model;
					}
					previewPanel.webview.postMessage({ 
						command: 'update',
						json,
						content,
						stale
					});
				});
		}
	}, DEBOUNCE_WAIT));

	window.onDidChangeActiveTextEditor((e) => {
        if ((previewPanel && window.activeTextEditor) && (e.document === window.activeTextEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			activeEditor = window.activeTextEditor;
			const content = activeEditor.document.getText();
			parseContent(langClient, content)
				.then((body) => {
					let stale = true;
					let json;
					if (body.model) {
						stale = false;
						json = body.model;
					}
					previewPanel.webview.postMessage({ 
						command: 'update',
						json,
						content,
						stale
					});
				});
		}
	})

	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		
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
		activeEditor = editor;
		render(editor.document.getText(), langClient, resourceRoot)
			.then((html) => {
				previewPanel.webview.html = html;
			})
			.catch((html) => {
				previewPanel.webview.html = html;
			});
		// Handle messages from the webview
        previewPanel.webview.onDidReceiveMessage(message => {
            switch (message.command) {
				case 'update':
					if (activeEditor && activeEditor.document.fileName.endsWith('.bal')) {
						const content = message.content;
						const replaceRange = new Range(
							activeEditor.document.positionAt(0),
							activeEditor.document.positionAt(activeEditor.document.getText().length)
						)
						preventDiagramUpdate = true;
						activeEditor.edit((edit) => edit.replace(replaceRange, content));
						preventDiagramUpdate = false;
					}
                    return;
            }
        }, undefined, context.subscriptions);
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