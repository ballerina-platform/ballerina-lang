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
import { workspace, commands, window, ViewColumn, ExtensionContext, TextEditor, WebviewPanel, TextDocumentChangeEvent, Uri } from 'vscode';
import { ExtendedLangClient } from '../core/extended-language-client';
import * as _ from 'lodash';
import { apiEditorRender } from './renderer';
import { BallerinaExtension } from '../core';
import { API_DESIGNER_NO_SERVICE } from '../core/messages';
import { WebViewRPCHandler, WebViewMethod } from '../utils';
import { join } from "path";

const DEBOUNCE_WAIT = 500;

let oasEditorPanel: WebviewPanel | undefined;
let activeEditor: TextEditor | undefined;
let preventAPIDesignerUpdate = false;

function updateOASWebView(docUri: Uri, resp: string, stale: boolean): void {
	if (oasEditorPanel) {
		oasEditorPanel.webview.postMessage({ 
			command: 'update',
			docUri: docUri.toString(),
			json: resp,
			stale
		});
	}
}

function showAPIEditorPanel(context: ExtensionContext, langClient: ExtendedLangClient, serviceName: string) : any {

    workspace.onDidChangeTextDocument(
        _.debounce((e: TextDocumentChangeEvent) => {
            if (activeEditor && (e.document === activeEditor.document) &&
            e.document.fileName.endsWith('.bal')) {
                if (preventAPIDesignerUpdate) {
                    preventAPIDesignerUpdate = false;
                    return;
                }

                const docUri = e.document.uri;
                if(oasEditorPanel){
                    langClient.getBallerinaOASDef(docUri, oasEditorPanel.title.split('-')[1].trim()).then((resp)=>{
                        if(resp.ballerinaOASJson !== undefined) {
                            updateOASWebView(docUri, JSON.stringify(resp.ballerinaOASJson), false);
                            preventAPIDesignerUpdate = true;
                        }
                    });
                }
            }
    }, DEBOUNCE_WAIT));

    window.onDidChangeActiveTextEditor(
        (activatedEditor: TextEditor | undefined) => {
            if (window.activeTextEditor && activatedEditor 
                && (activatedEditor.document === window.activeTextEditor.document) 
                && activatedEditor.document.fileName.endsWith('.bal')
                && activatedEditor !== activeEditor) {
                    activeEditor = window.activeTextEditor;

                    if(oasEditorPanel){
                        langClient.getServiceListForActiveFile(activeEditor.document.uri).then((resp) => {
                            if(resp.services && resp.services.length > 1) {
                                window.showQuickPick(resp.services).then((selected) => {
                                    if(selected && activeEditor){
                                        const html = apiEditorRender(context, langClient, activeEditor.document.uri, selected);
                                        if (oasEditorPanel && html) {
                                            oasEditorPanel.webview.html = html;
                                            oasEditorPanel.title ="Ballerina API Designer - " + selected;
                                        }
                                    }
                                });
                            } else {
                                if(activeEditor){
                                    const html = apiEditorRender(context, langClient, activeEditor.document.uri, resp.services[0]);
                                    if (oasEditorPanel && html) {
                                        oasEditorPanel.webview.html = html;
                                        oasEditorPanel.title ="Ballerina API Designer - " + resp.services[0];
                                    }
                                }
                            }
                        });
                    }
            }
    });
    
    const editor = window.activeTextEditor;

    // TODO : proper handler if not the active editor
    if (!editor) {
        return "";
    }
    activeEditor = editor;

    let executeCreateAPIEditor = function (serviceName: string) {
        let renderHtml = apiEditorRender(context,
            langClient, editor.document.uri, serviceName);
        createAPIEditorPanel(serviceName, renderHtml, langClient, context);
    };

    if (serviceName) {
        executeCreateAPIEditor(serviceName);
    } else {
        langClient.getServiceListForActiveFile(activeEditor.document.uri).then(resp => {
            if (resp.services.length === 0) {
                window.showInformationMessage(API_DESIGNER_NO_SERVICE);
            } else if (resp.services && resp.services.length > 1) {
                window.showQuickPick(resp.services).then(service => {
                    if (service && activeEditor) {
                        executeCreateAPIEditor(service);
                    }
                });
            } else {
                executeCreateAPIEditor(resp.services[0]);
            }
        });
    }
}

function createAPIEditorPanel(selectedService: string, renderHtml: string,
    langClient: ExtendedLangClient, context: ExtensionContext) : any {

    if (!oasEditorPanel) {
        oasEditorPanel = window.createWebviewPanel(
            'ballerinaOASEditor',
            'Ballerina API Designer - ' + selectedService,
            { viewColumn: ViewColumn.Two, preserveFocus: true } ,
            {
                enableScripts: true,
                retainContextWhenHidden: true,
            }
        );
    }

    oasEditorPanel.webview.html = renderHtml;

    const remoteMethods: WebViewMethod[] = [
        {
            methodName: "getSwaggerDef",
            handler: (args: any[]): Thenable<any> => {
                return langClient.getBallerinaOASDef(args[0], args[1]);
            }
        },{
            methodName: 'triggerSwaggerDefChange',
            handler: (args: any[]) => {
                return langClient.triggerSwaggerDefChange(args[0], args[1]);
            }
        }
    ];
    WebViewRPCHandler.create(oasEditorPanel.webview, langClient, remoteMethods);

    oasEditorPanel.iconPath = {
		light: Uri.file(join(context.extensionPath, 'resources/images/icons/api-design.svg')),
		dark: Uri.file(join(context.extensionPath, 'resources/images/icons/api-design-inverse.svg'))
	};

    oasEditorPanel.webview.onDidReceiveMessage(message => {
        switch (message.command) {
            case 'oasASTModified' :
                if(activeEditor && activeEditor.document.fileName.endsWith('.bal')) {
                    preventAPIDesignerUpdate = true;
                }
                return;
        }
    }, undefined, context.subscriptions);

    oasEditorPanel.onDidDispose(() => {
        oasEditorPanel = undefined;
    });
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
    let langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;
    const showAPIRenderer = commands.registerCommand('ballerina.showAPIEditor', serviceNameArg => {
        ballerinaExtInstance.onReady()
        .then(() => {
            const { experimental } = langClient.initializeResult!.capabilities;
            const serverProvidesAPIEditorFeature = experimental && experimental.apiEditorProvider;

            if (!serverProvidesAPIEditorFeature) {
                ballerinaExtInstance.showMessageServerMissingCapability();
                return;
            }
            showAPIEditorPanel(context, langClient, (serviceNameArg) ? serviceNameArg.argumentV : "");
        })
		.catch((e) => {
			if (!ballerinaExtInstance.isValidBallerinaHome()) {
				ballerinaExtInstance.showMessageInvalidBallerinaHome();
			} else {
				ballerinaExtInstance.showPluginActivationError();
			}
		});
    });
    
    context.subscriptions.push(showAPIRenderer);
}