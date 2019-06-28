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