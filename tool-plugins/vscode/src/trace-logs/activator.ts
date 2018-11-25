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

import { commands, window, ViewColumn, ExtensionContext, WebviewPanel, StatusBarAlignment, StatusBarItem } from 'vscode';
import { render, renderDetailView } from './renderer';
import { ExtendedLangClient } from '../core/extended-language-client';
import { BallerinaExtension } from '../core';
import { WebViewRPCHandler, WebViewMethod } from '../utils';
import Traces from './traces';

let traceLogsPanel: WebviewPanel | undefined;
let traceDetailsPanel: WebviewPanel | undefined;
let traces: Traces = new Traces();
let status: StatusBarItem | undefined;

function showTraces(context: ExtensionContext, langClient: ExtendedLangClient) {

    if (traceLogsPanel) {
        traceLogsPanel.dispose();
    }
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

    const html = render(context, langClient);
    if (traceLogsPanel && html) {
        traceLogsPanel.webview.html = html;
        traceLogsPanel.webview.postMessage({
            command: 'updateTraces',
        });
    }

    const remoteMethods: WebViewMethod[] = [
        {
            methodName: 'showDetails',
            handler: (trace: any) => {
                if (traceDetailsPanel) {
                    traceDetailsPanel.reveal(traceDetailsPanel.viewColumn);
                    const html = renderDetailView(context, langClient, trace);
                    traceDetailsPanel.webview.html = html;
                    return Promise.resolve();
                }
                // TODO: optimize this logic to reuse existing traceDetailsPanel
                traceDetailsPanel = window.createWebviewPanel(
                    'ballerinaNetworkLogsDetails',
                    "Ballerina Network Log Details",
                    { viewColumn: ViewColumn.Beside } ,
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
                return Promise.resolve(traces.getTraces());
            }
        },
        {
            methodName: 'clearLogs',
            handler: () => {
                traces = new Traces();
                updateStatus();
                if (traceLogsPanel && traceLogsPanel.webview) {
                    traceLogsPanel.webview.postMessage({
                        command: 'updateTraces',
                    });
                }
                return Promise.resolve();
            }
        },
        
    ];

    WebViewRPCHandler.create(traceLogsPanel.webview, langClient, remoteMethods);

    traceLogsPanel.onDidDispose(() => {
        traceDetailsPanel!.dispose();
        traceLogsPanel = undefined;
	});
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
    let langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;

    status = window.createStatusBarItem(StatusBarAlignment.Left, 100);
	status.command = 'ballerina.showTraces';
    context.subscriptions.push(status);
    updateStatus();
    ballerinaExtInstance.onReady().then(() => {
        langClient.onNotification('window/traceLogs', (trace: Object) => {
            addNewTrace(trace);
            updateStatus();
            if (traceLogsPanel && traceLogsPanel.webview) {
                traceLogsPanel.webview.postMessage({
                    command: 'updateTraces',
                });
            }
        });
    })
    .catch((e) => {
        window.showErrorMessage('Could not start network logs feature',e.message);
    });

    const traceRenderer = commands.registerCommand('ballerina.showTraces', () => {
        showTraces(context, langClient);
    });
    
    context.subscriptions.push(traceRenderer);
}

function addNewTrace(trace: any) {
    traces.addTrace(trace);
}

function updateStatus() {
    if (!status) {
        return;
    }
    const count = traces.getTraces().length;
    status!.text = `$(mirror) ${count} Ballerina network logs`;
    status!.show();
}
