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
import { workspace, commands, window, Uri, ViewColumn, ExtensionContext, TextEditor, WebviewPanel, TextDocumentChangeEvent } from 'vscode';
import * as _ from 'lodash';
import { render } from './renderer';
import { ExtendedLangClient } from '../core/extended-language-client';
import { BallerinaExtension } from '../core';
import { WebViewRPCHandler } from '../utils';

const DEBOUNCE_WAIT = 500;

let diagramViewPanel: WebviewPanel | undefined;
let activeEditor: TextEditor | undefined;
let preventDiagramUpdate = false;
let rpcHandler: WebViewRPCHandler;

function updateWebView(docUri: Uri): void {
	if (rpcHandler) {
		rpcHandler.invokeRemoteMethod("updateAST", [docUri.toString()], () => {});
	}
}

function showDiagramEditor(context: ExtensionContext, langClient: ExtendedLangClient): void {
	const didChangeDisposable = workspace.onDidChangeTextDocument(
			_.debounce((e: TextDocumentChangeEvent) => {
		if (activeEditor && (e.document === activeEditor.document) &&
			e.document.fileName.endsWith('.bal')) {
			if (preventDiagramUpdate) {
				return;
			}
			updateWebView( e.document.uri);
		}
	}, DEBOUNCE_WAIT));

	const changeActiveEditorDisposable =  window.onDidChangeActiveTextEditor(
			(activatedEditor: TextEditor | undefined) => {
		if (window.activeTextEditor && activatedEditor 
					&& (activatedEditor.document === window.activeTextEditor.document) 
					&& activatedEditor.document.fileName.endsWith('.bal')) {
			activeEditor = window.activeTextEditor;
			updateWebView(activatedEditor.document.uri);
		}
	});

	if (diagramViewPanel) {
		diagramViewPanel.reveal(ViewColumn.Two, true);
		return;
	}
	// Create and show a new webview
	diagramViewPanel = window.createWebviewPanel(
		'ballerinaDiagram',
		"Ballerina Diagram",
		{ viewColumn: ViewColumn.Two, preserveFocus: true } ,
		{
			enableScripts: true,
			retainContextWhenHidden: true,
		}
	);
	const editor = window.activeTextEditor;
	if(!editor) {
		return;
	}
	activeEditor = editor;
	rpcHandler = WebViewRPCHandler.create(diagramViewPanel.webview, langClient);
	const html = render(context, langClient, editor.document.uri);
	if (diagramViewPanel && html) {
		diagramViewPanel.webview.html = html;
	}
	// Handle messages from the webview
	diagramViewPanel.webview.onDidReceiveMessage(message => {
		switch (message.command) {
			case 'astModified':
				if (activeEditor && activeEditor.document.fileName.endsWith('.bal')) {
					preventDiagramUpdate = true;
					const ast = JSON.parse(message.ast);
					langClient.triggerASTDidChange(ast, activeEditor.document.uri)
						.then(() => {
							preventDiagramUpdate = false;
						});	
				}
				return;
		}
	}, undefined, context.subscriptions);

	diagramViewPanel.onDidDispose(() => {
		diagramViewPanel = undefined;
		didChangeDisposable.dispose();
		changeActiveEditorDisposable.dispose();
	});
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
    let langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;
	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		return ballerinaExtInstance.onReady()
		.then(() => {
			const { experimental } = langClient.initializeResult!.capabilities;
			const serverProvidesAST = experimental && experimental.astProvider;

			if (!serverProvidesAST) {
				ballerinaExtInstance.showMessageServerMissingCapability();
				return {};
			}
			showDiagramEditor(context, langClient);
		})
		.catch((e) => {
			if (!ballerinaExtInstance.isValidBallerinaHome()) {
				ballerinaExtInstance.showMessageInvalidBallerinaHome();
			} else {
				ballerinaExtInstance.showPluginActivationError();
			}
		});
	});

	context.subscriptions.push(diagramRenderDisposable);
}
