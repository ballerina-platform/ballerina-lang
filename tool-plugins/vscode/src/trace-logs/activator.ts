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

import { commands, window, ViewColumn, ExtensionContext, WebviewPanel } from 'vscode';
import { render, renderDetailView } from './renderer';
import { ExtendedLangClient } from '../core/extended-language-client';
import { BallerinaExtension } from '../core';
import { WebViewRPCHandler } from '../utils';

let traceLogsPanel: WebviewPanel | undefined;
let traceDetailsPanel: WebviewPanel | undefined;
let traces: Array<object> = [];

function showTraces(context: ExtensionContext, langClient: ExtendedLangClient) {
    if (traceLogsPanel) {
        traceLogsPanel.reveal(undefined, true);
    } else {
        // Create and show a new webview
        traceLogsPanel = window.createWebviewPanel(
            'ballerinaNetworkLogs',
            "Ballerina Network logs",
            { viewColumn: ViewColumn.Two, preserveFocus: true } ,
            {
                enableScripts: true,
                retainContextWhenHidden: false,
            }
        );
    }

    const html = render(context, langClient);
    if (traceLogsPanel && html) {
        traceLogsPanel.webview.html = html;
        traceLogsPanel.webview.postMessage({
            command: 'updateTraces',
        });
    }

    WebViewRPCHandler.create([
        {
            methodName: 'showDetails',
            handler: (trace: any) => {
                // if (traceDetailsPanel) {
                //     traceDetailsPanel.reveal(undefined, true);
                //     const html = renderDetailView(context, langClient, trace);
                //     traceDetailsPanel.webview.html = html;
                //     return Promise.resolve();
                // }
                // TODO: optimize this logic to reuse existing traceDetailsPanel
                traceDetailsPanel = window.createWebviewPanel(
                    'ballerinaNetworkLogsDetails',
                    "Ballerina Network Log Details",
                    { viewColumn: ViewColumn.Beside, preserveFocus: true } ,
                    {
                        enableScripts: true,
                        retainContextWhenHidden: false,
                    }
                );
                const html = renderDetailView(context, langClient, trace);
                traceDetailsPanel.webview.html = html;
                traceDetailsPanel.onDidDispose(() => {
                    traceDetailsPanel = undefined;
                });
                return Promise.resolve();
            }
        },
        {
            methodName: 'getTraces',
            handler: (args: Array<Object>) => {
                return Promise.resolve(traces);
            }
        },
        {
            methodName: 'clearLogs',
            handler: () => {
                traces = [];
                if (traceLogsPanel && traceLogsPanel.webview) {
                    traceLogsPanel.webview.postMessage({
                        command: 'updateTraces',
                    });
                }
                return Promise.resolve();
            }
        },
        
    ], 
        traceLogsPanel.webview
    );

    traceLogsPanel.onDidDispose(() => {
        traceDetailsPanel!.dispose();
	});
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
    let langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;

    const examplesListRenderer = commands.registerCommand('ballerina.showTraces', () => {
        ballerinaExtInstance.onReady()
        .then(() => {
            const { experimental } = langClient.initializeResult!.capabilities;
            const serverProvidesExamples = experimental && experimental.examplesProvider;

            if (!serverProvidesExamples) {
                ballerinaExtInstance.showMessageServerMissingCapability();
                return;
            }

            showTraces(context, langClient);
        })
		.catch((e) => {
			if (!ballerinaExtInstance.isValidBallerinaHome()) {
				ballerinaExtInstance.showMessageInvalidBallerinaHome();
			} else {
				ballerinaExtInstance.showPluginActivationError();
			}
		});
    });

    ballerinaExtInstance.onReady().then(()=>{
        langClient.onNotification('window/traceLogs', (trace: Object) => {
            traces.push(trace);
            if (traceLogsPanel && traceLogsPanel.webview) {
                traceLogsPanel.webview.postMessage({
                    command: 'updateTraces',
                });
            }
        });
    });
    
    context.subscriptions.push(examplesListRenderer);
}