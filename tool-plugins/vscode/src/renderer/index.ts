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
import { workspace, commands, window, Uri, ViewColumn, ExtensionContext, TextEditor, WebviewPanel, TextDocumentChangeEvent, Position, Range, Selection } from 'vscode';
import * as _ from 'lodash';
import { StaticProvider } from './content-provider';
import { render } from './renderer';
import { apiEditorRender } from './api-editor-renderer';
import { BallerinaAST, ExtendedLangClient } from '../lang-client';
import { WebViewRPCHandler } from '../utils';
import BallerinaExtension from '../core/ballerina-extension';

const DEBOUNCE_WAIT = 500;

let previewPanel: WebviewPanel | undefined;
let oasEditorPanel: WebviewPanel | undefined;
let activeEditor: TextEditor | undefined;
let preventDiagramUpdate = false;

function updateWebView(ast: BallerinaAST, docUri: Uri, stale: boolean): void {
	if (previewPanel) {
		previewPanel.webview.postMessage({ 
			command: 'update',
			json: ast,
			docUri: docUri.toString(),
			stale
		});
	}
}

export function activate(context: ExtensionContext, langClient: ExtendedLangClient) {
	const { experimental } = langClient.initializeResult!.capabilities;
	const serverProvidesAST = experimental && experimental.astProvider;

    if (!serverProvidesAST) {
        commands.registerCommand('ballerina.showDiagram', () => {
            BallerinaExtension.showMessageServerMissingCapability();
        });
        return;
    }

	workspace.onDidChangeTextDocument(_.debounce((e: TextDocumentChangeEvent) => {
        if (activeEditor && (e.document === activeEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
			if (preventDiagramUpdate) {
				return;
			}
			const docUri = e.document.uri;
			langClient.getAST(docUri)
				.then((resp) => {
					let stale = true;
					if (resp.ast) {
						stale = false;
						updateWebView(resp.ast, docUri, stale);
					}
				});
		}
	}, DEBOUNCE_WAIT));

	window.onDidChangeActiveTextEditor((activatedEditor: TextEditor | undefined) => {
		if (window.activeTextEditor && activatedEditor 
					&& (activatedEditor.document === window.activeTextEditor.document) 
					&& activatedEditor.document.fileName.endsWith('.bal')) {
			activeEditor = window.activeTextEditor;
			const docUri = activatedEditor.document.uri;
			langClient.getAST(docUri)
				.then((resp) => {
					let stale = true;
					if (resp.ast) {
						stale = false;
						updateWebView(resp.ast, docUri, stale);
					}
				});
		}
	});

	commands.registerCommand('ballerina.showAPIEditor', () => {
		if(!oasEditorPanel) {
			oasEditorPanel = window.createWebviewPanel(
				'ballerinaOASEditor',
				"Ballerina API Editor",
				{ viewColumn: ViewColumn.Two, preserveFocus: true } ,
				{
					enableScripts: true,
					retainContextWhenHidden: true,
				}
			);
		}
		const editor = window.activeTextEditor;
		if(!editor) {
            return "";
		}
		activeEditor = editor;
		WebViewRPCHandler.create([{
			methodName: 'getSwaggerDef',
			handler: (args: any[]) => {
				return langClient.getBallerinaOASDef(args[0], args[1]);
			}
		},{
			methodName: 'onOasChange',
			handler: (args: any[]) => {
				return langClient.getBallerinaASTforOas(args[0]);
			}
		}], oasEditorPanel.webview)

		langClient.getServiceListForActiveFile(activeEditor.document.uri).then((resp) => {
			if(resp.services) {
				window.showQuickPick(resp.services).then((selected) => {
					if(selected && activeEditor){
						const html = apiEditorRender(context, langClient, editor.document.uri, selected);
						if (oasEditorPanel && html) {
							oasEditorPanel.webview.html = html;
						}
					}
				});
				
			}
		})
	});

	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		if (previewPanel) {
			previewPanel.reveal(ViewColumn.Two, true);
			return;
		}
		// Create and show a new webview
        previewPanel = window.createWebviewPanel(
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
            return "";
		}
		activeEditor = editor;
		WebViewRPCHandler.create([
			{
				methodName: 'getAST',
				handler: (args: any[]) => {
					return langClient.getAST(args[0]);
				}
			},
			{
				methodName: 'getEndpoints',
				handler: (args: any[]) => {
					return langClient.getEndpoints();
				}
			},
			{
				methodName: 'parseFragment',
				handler: (args: any[]) => {
					return langClient.parseFragment({
						enclosingScope: args[0].enclosingScope,
						expectedNodeType: args[0].expectedNodeType,
						source: args[0].source
					});
				}
			},
			{
				methodName: 'revealRange',
				handler: (args: any[]) => {
					if (activeEditor) {
						const start = new Position(args[0] - 1, args[1] - 1);
						const end = new Position(args[2] - 1, args[3]);
						activeEditor.revealRange(new Range(start, end));
						activeEditor.selection = new Selection(start, end);
					}
					return Promise.resolve();
				}
			}
		], previewPanel.webview);
		const html = render(context, langClient, editor.document.uri);
		if (previewPanel && html) {
			previewPanel.webview.html = html;
		}
		// Handle messages from the webview
        previewPanel.webview.onDidReceiveMessage(message => {
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
		previewPanel.onDidDispose(() => {
			previewPanel = undefined;
		});
    });
	context.subscriptions.push(diagramRenderDisposable);
}

export function errored(context: ExtensionContext) {
	const provider = new StaticProvider();
	let registration = workspace.registerTextDocumentContentProvider('ballerina-static', provider);

	const diagramRenderDisposable = commands.registerCommand('ballerina.showDiagram', () => {
		let uri = Uri.parse('ballerina-static:///resources/pages/error.html');
		commands.executeCommand('vscode.previewHtml', uri, ViewColumn.Two, 'Ballerina Diagram');
	});

	context.subscriptions.push(diagramRenderDisposable, registration);
}