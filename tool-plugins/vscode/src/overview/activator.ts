import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';

import { BallerinaExtension, ExtendedLangClient } from 'src/core';
import { ExtensionContext, commands, window, Uri, ViewColumn } from 'vscode';

import {render} from './renderer';
import { WebViewRPCHandler } from '../utils';

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

            const overviewPanel = window.createWebviewPanel(
                'projectOverview',
                'Project Overview',
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

			const handler = WebViewRPCHandler.create(overviewPanel.webview, langClient);
            const html = render(context, langClient, Uri.parse(sourceRoot).toString());
		    overviewPanel.webview.html = html;
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