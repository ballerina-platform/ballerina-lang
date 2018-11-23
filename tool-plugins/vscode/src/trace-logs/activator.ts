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
import { WebViewRPCHandler } from '../utils';

let traceLogsPanel: WebviewPanel | undefined;
let traceDetailsPanel: WebviewPanel | undefined;
let traces: Array<object> = [];
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

    WebViewRPCHandler.create([
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
                return Promise.resolve(traces);
            }
        },
        {
            methodName: 'clearLogs',
            handler: () => {
                traces = [];
                updateStatus();
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
    traces.push(trace);
    traces = filterEmptyLogs(mergeRelatedMessages(traces));
}

function updateStatus() {
    if (!status) {
        return;
    }
    const count = traces.length;
    status!.text = `$(mirror) ${count} Ballerina network logs`;
    status!.show();
}

function mergeRelatedMessages(traces: Array<any>) {
    const newTraces = [];
    if (traces.length < 2) {
        return traces;
    }
    for (let index = 0; index < traces.length; index++) {
        let record1 = traces[index];
        let record2 = traces[index + 1];
        if (record1.message.headerType.startsWith('DefaultHttpRequest')
            && record2
            && record1.thread === record2.thread
            && (record2.message.headerType.startsWith('DefaultLastHttpContent')
                || record2.message.headerType.startsWith('EmptyLastHttpContent'))) {
            record1.message.payload = record2.message.payload;
            record1.message.payload = record1.message.payload ? record1.message.payload : record2.message.headers;
            newTraces.push(record1);
        } else if (record1.message.headerType.startsWith('DefaultLastHttpContent') ||
            record1.message.headerType.startsWith('EmptyLastHttpContent')) {
            // do nothing
        } else {
            newTraces.push(record1);
        }
    }
    return newTraces;
}

function filterEmptyLogs(traces: Array<any>) {
    return traces.filter((trace: any) => {
        if (trace.message.headers.trim() === "" && trace.message.payload.trim() === "") {
            return false;
        }
        const direction = trace.message.direction || "";
        return direction.length > 0;
    });
}