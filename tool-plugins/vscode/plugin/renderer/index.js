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

const { DiagramProvider, StaticProvider } = require('./content-provider');
let diagramProvider;
const DEBOUNCE_WAIT = 500;

exports.activate = function(context) {
	const provider = diagramProvider = new DiagramProvider();

	workspace.onDidChangeTextDocument(_.debounce((e) => {
        if ((window.activeTextEditor) && (e.document === window.activeTextEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			provider.update(Uri.parse('ballerina-diagram:///ballerina/diagram'));
		}
	}, DEBOUNCE_WAIT));

	window.onDidChangeActiveTextEditor((e) => {
        if ((window.activeTextEditor) && (e.document === window.activeTextEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			provider.update(Uri.parse('ballerina-diagram:///ballerina/diagram'));
		}
	})

	let registration = workspace.registerTextDocumentContentProvider('ballerina-diagram', provider);

	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		let uri = Uri.parse('ballerina-diagram:///ballerina/diagram');
		commands.executeCommand('vscode.previewHtml', uri, ViewColumn.Two, 'Ballerina Diagram');
	});

	provider.activate();
	context.subscriptions.push(diagramRenderDisposable, registration);
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