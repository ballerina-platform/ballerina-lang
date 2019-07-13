/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';
import * as _ from 'lodash';

import { BallerinaExtension, ExtendedLangClient } from 'src/core';
import { ExtensionContext, commands, window, Uri, ViewColumn, TextDocumentChangeEvent, 
	workspace, WebviewPanel } from 'vscode';

import {render} from './renderer';
import { WebViewRPCHandler, getCommonWebViewOptions } from '../utils';

const DEBOUNCE_WAIT = 500;

let overviewPanel: WebviewPanel | undefined;
let rpcHandler: WebViewRPCHandler;

function updateWebView(docUri: Uri): void {
	if (rpcHandler) {
		rpcHandler.invokeRemoteMethod("updateAST", [docUri.toString()], () => {});
	}
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
	let langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;

	const projectOverviewDisposable = commands.registerCommand('ballerina.showProjectOverview', () => {
		return ballerinaExtInstance.onReady()
		.then(() => {
			if (!window.activeTextEditor) {
				return;
			}
			const currentUri = window.activeTextEditor.document.fileName;
			const sourceRoot = getSourceRoot(currentUri, path.parse(currentUri).root);

			if (!sourceRoot) {
				return;
			}

			const didChangeDisposable = workspace.onDidChangeTextDocument(
				_.debounce((e: TextDocumentChangeEvent) => {
				updateWebView( e.document.uri);
			}, DEBOUNCE_WAIT));

            overviewPanel = window.createWebviewPanel(
                'projectOverview',
                'Project Overview',
                { viewColumn: ViewColumn.One, preserveFocus: true } ,
                getCommonWebViewOptions()
			);

            const editor = window.activeTextEditor;
            if(!editor) {
                return;
            }

			rpcHandler = WebViewRPCHandler.create(overviewPanel, langClient);
			const html = render(context, langClient, Uri.file(sourceRoot).toString());
			if (overviewPanel && html) {
				overviewPanel.webview.html = html;
			}
			
			overviewPanel.onDidDispose(() => {
				overviewPanel = undefined;
				didChangeDisposable.dispose();
			});
		});
	});
    context.subscriptions.push(projectOverviewDisposable);
}

function getSourceRoot(currentPath: string, root: string): string|undefined {
	if (fs.existsSync(path.join(currentPath, '.ballerina'))) {
		if (currentPath !== os.homedir()) {
			return currentPath;
		}
	}

	if (currentPath === root) {
		return;
	}

	return getSourceRoot(path.dirname(currentPath), root);
}