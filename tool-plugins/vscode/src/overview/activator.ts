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
import * as _ from 'lodash';

import { BallerinaExtension, ExtendedLangClient, ConstructIdentifier, ballerinaExtInstance } from '../core';
import { ExtensionContext, commands, window, Uri, ViewColumn, TextDocumentChangeEvent, 
	workspace, WebviewPanel } from 'vscode';

import { render } from './renderer';
import { WebViewRPCHandler, getCommonWebViewOptions } from '../utils';
import { TM_EVENT_OPEN_FILE_OVERVIEW, CMP_FILE_OVERVIEW } from '../telemetry';

const DEBOUNCE_WAIT = 500;

let projectOverviewPanel: WebviewPanel | undefined;
let fileOverviewPanel: WebviewPanel | undefined;
let projectOverviewRpcHandler: WebViewRPCHandler;
let fileOverviewRpcHandler: WebViewRPCHandler;

function updateProjectOverview(docUri: Uri): void {
	if (projectOverviewRpcHandler) {
		projectOverviewRpcHandler.invokeRemoteMethod("updateAST", [docUri.toString()], () => {});
	}
}

function updateFileOverview(docUri: Uri): void {
	if (fileOverviewRpcHandler) {
		fileOverviewRpcHandler.invokeRemoteMethod("updateAST", [docUri.toString()], () => {});
	}
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
	const reporter = ballerinaExtInstance.telemetryReporter;
    const context = <ExtensionContext> ballerinaExtInstance.context;
	const langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;

	function updateSelectedConstruct(construct: ConstructIdentifier) {
		// If Project Overview is already showing update it to show the selected construct
		if (projectOverviewPanel) {
			if (projectOverviewRpcHandler) {
				const { sourceRoot, filePath, moduleName, constructName, subConstructName } = construct;
				projectOverviewRpcHandler.invokeRemoteMethod("selectConstruct", [
					sourceRoot, filePath, moduleName, constructName, subConstructName], () => {});
			}
		} else {
			// If Project Overview is not yet opened open it and show the selected construct
			openProjectOverview(langClient, construct);
		}
	}

	ballerinaExtInstance.onProjectTreeElementClicked((construct) => {
		if (projectOverviewPanel) {
			projectOverviewPanel.reveal();
		}
		updateSelectedConstruct(construct);
	});

	const fileOverviewDisposable = commands.registerCommand('ballerina.showFileOverview', () => {
		reporter.sendTelemetryEvent(TM_EVENT_OPEN_FILE_OVERVIEW, { component: CMP_FILE_OVERVIEW });
		return ballerinaExtInstance.onReady()
		.then(() => {
			openFileOverview(langClient);
		}).catch((e) => {
			reporter.sendTelemetryException(e, { component: CMP_FILE_OVERVIEW });
		});
	});

    context.subscriptions.push(fileOverviewDisposable);
}

function openProjectOverview(langClient: ExtendedLangClient, construct: ConstructIdentifier) {
	if (!projectOverviewPanel) {
		projectOverviewPanel = window.createWebviewPanel(
			'projectOverview',
			'Project Overview',
			{ viewColumn: ViewColumn.One, preserveFocus: true },
			getCommonWebViewOptions()
		);

		ballerinaExtInstance.addWebviewPanel("overview", projectOverviewPanel);
	}

	projectOverviewRpcHandler = WebViewRPCHandler.create(projectOverviewPanel, langClient);
	const html = render(construct);
	if (projectOverviewPanel && html) {
		projectOverviewPanel.webview.html = html;
	}

	const didChangeDisposable = workspace.onDidChangeTextDocument(
		_.debounce((e: TextDocumentChangeEvent) => {
		updateProjectOverview(e.document.uri);
	}, DEBOUNCE_WAIT));

	projectOverviewPanel.onDidDispose(() => {
		projectOverviewPanel = undefined;
		didChangeDisposable.dispose();
	});
}

function openFileOverview(langClient: ExtendedLangClient) {
	if (!window.activeTextEditor) {
		return;
	}

	const didChangeDisposable = workspace.onDidChangeTextDocument(
		_.debounce((e: TextDocumentChangeEvent) => {
		updateFileOverview(e.document.uri);
	}, DEBOUNCE_WAIT));

	const didChangeActiveEditorDisposable = window.onDidChangeActiveTextEditor((activeEditor) => {
		if (!(activeEditor && activeEditor.document && activeEditor.document.languageId === "ballerina")) {
			return;
		}
		didChangeDisposable.dispose();
		didChangeActiveEditorDisposable.dispose();
		openFileOverview(langClient);
	});

	if (!fileOverviewPanel) {
		fileOverviewPanel = window.createWebviewPanel(
			'fileOverview',
			'File Overview',
			{ viewColumn: ViewColumn.Two, preserveFocus: true },
			getCommonWebViewOptions()
		);

		ballerinaExtInstance.addWebviewPanel('file', fileOverviewPanel);
	}

	fileOverviewRpcHandler = WebViewRPCHandler.create(fileOverviewPanel, langClient);
	const html = render({filePath: window.activeTextEditor.document.uri.toString(), constructName: "", moduleName: ""});
	if (fileOverviewPanel && html) {
		fileOverviewPanel.webview.html = html;
	}

	fileOverviewPanel.onDidDispose(() => {
		fileOverviewPanel = undefined;
		didChangeDisposable.dispose();
		didChangeActiveEditorDisposable.dispose();
	});
}
