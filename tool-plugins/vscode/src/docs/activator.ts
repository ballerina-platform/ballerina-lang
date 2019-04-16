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
import {
	workspace, commands, window, Uri, ViewColumn,
	ExtensionContext, TextEditor, WebviewPanel, TextDocumentChangeEvent
} from 'vscode';
import * as _ from 'lodash';
import { render } from './renderer';
import { BallerinaAST, ExtendedLangClient } from '../core/extended-language-client';
import { BallerinaExtension } from '../core';

const DEBOUNCE_WAIT = 500;

let previewPanel: WebviewPanel | undefined;
let activeEditor: TextEditor | undefined;

function updateWebView(ast: BallerinaAST, docUri: Uri): void {
	if (previewPanel) {
		previewPanel.webview.postMessage({
			command: 'update',
			json: ast,
			docUri: docUri.toString()
		});
	}
}

function showDocs(context: ExtensionContext, langClient: ExtendedLangClient, nodeName: string): void {
	const didChangeDisposable = workspace.onDidChangeTextDocument(
		_.debounce((e: TextDocumentChangeEvent) => {
			if (activeEditor && (e.document === activeEditor.document) &&
				e.document.fileName.endsWith('.bal')) {
				const docUri = e.document.uri;
				langClient.getAST(docUri)
					.then((resp) => {
						if (resp.ast) {
							updateWebView(resp.ast, docUri);
						}
					});
			}
		}, DEBOUNCE_WAIT));

	const changeActiveEditorDisposable = window.onDidChangeActiveTextEditor(
		(activatedEditor: TextEditor | undefined) => {
			if (window.activeTextEditor && activatedEditor
				&& (activatedEditor.document === window.activeTextEditor.document)
				&& activatedEditor.document.fileName.endsWith('.bal')) {
				activeEditor = window.activeTextEditor;
				const docUri = activatedEditor.document.uri;
				langClient.getAST(docUri)
					.then((resp) => {
						if (resp.ast) {
							updateWebView(resp.ast, docUri);
						}
					});
			}
		});

	if (previewPanel) {
		previewPanel.reveal(ViewColumn.Two, true);
		scrollToTitle(previewPanel, nodeName);
		return;
	}
	// Create and show a new webview
	previewPanel = window.createWebviewPanel(
		'ballerinaDocs',
		"Ballerina Docs",
		{ viewColumn: ViewColumn.Two, preserveFocus: true },
		{
			enableScripts: true,
			retainContextWhenHidden: true,
		}
	);
	const editor = window.activeTextEditor;
	if (!editor) {
		return;
	}
	activeEditor = editor;

	const html = render(context, langClient);
	
	if (previewPanel && html) {
		previewPanel.webview.html = html;
	}

	const disposeLoaded = previewPanel.webview.onDidReceiveMessage((e) => {
		if (e.message !== "loaded-doc-preview") {
			return;
		}
		disposeLoaded.dispose();
		langClient.getAST(editor.document.uri)
		.then((resp) => {
			if (resp.ast) {
				updateWebView(resp.ast, editor.document.uri);
				if (previewPanel) {
					scrollToTitle(previewPanel, nodeName);
				}
			}
		});
	});

	previewPanel.onDidDispose(() => {
		previewPanel = undefined;
		didChangeDisposable.dispose();
		changeActiveEditorDisposable.dispose();
	});
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
	let context = <ExtensionContext>ballerinaExtInstance.context;
	let langClient = <ExtendedLangClient>ballerinaExtInstance.langClient;
	const docsRenderDisposable = commands.registerCommand('ballerina.showDocs', nodeNameArg => {
		return ballerinaExtInstance.onReady()
			.then(() => {
				const { experimental } = langClient.initializeResult!.capabilities;
				const serverProvidesAST = experimental && experimental.astProvider;

				if (!serverProvidesAST) {
					ballerinaExtInstance.showMessageServerMissingCapability();
					return {};
				}
				showDocs(context, langClient, (nodeNameArg) ? nodeNameArg.argumentV : "");
			})
			.catch((e) => {
				if (!ballerinaExtInstance.isValidBallerinaHome()) {
					ballerinaExtInstance.showMessageInvalidBallerinaHome();
				} else {
					ballerinaExtInstance.showPluginActivationError();
				}
			});
	});

	context.subscriptions.push(docsRenderDisposable);
}

function scrollToTitle(previewPanel:WebviewPanel, anchorId: string){
	if (previewPanel && anchorId) {
		previewPanel.webview.postMessage({
			command: 'scroll',
			anchor: anchorId
		});
	}
}
