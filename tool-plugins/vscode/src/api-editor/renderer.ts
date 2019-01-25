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

import { ExtendedLangClient } from '../core/extended-language-client';
import { Uri, ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils/index';

export function apiEditorRender(context: ExtensionContext, langClient: ExtendedLangClient,
    docUri: Uri, selectedService: string, retries: number = 1) : string {

    const body = `
        <div class='api-container'>
            <div class='message'></div>
            <div class='api-visualizer' id='api-visualizer'></div>
        </div>
    `;
    const bodyCss = "api-designer";
    const styles = ``;
    const script = `
        function loadedScript() {
            let docUri = ${JSON.stringify(docUri.toString())};
            let updatedJSON = '';
            let selectedService = ${JSON.stringify(selectedService.toString())};

            // Handle the message inside the webview
            window.addEventListener('message', event => {
                let message = event.data; // The JSON data our extension sent
                switch (message.command) {
                    case 'update':
                        docUri = message.docUri;
                        updatedJSON = message.json
                        drawAPIEditor();
                        break;
                }
            });

            function getSwaggerJson(docUri, serviceName) {
                return new Promise((resolve, reject) => {
                    webViewRPCHandler.invokeRemoteMethod('getSwaggerDef', [docUri, serviceName], (resp) => {
                        resolve(resp);
                    });
                })
            }

            function onDidJsonChange(event, oasJson) {
                webViewRPCHandler.invokeRemoteMethod('triggerSwaggerDefChange', [JSON.stringify(oasJson), docUri]);
            }

            function drawAPIEditor() {
                if(updatedJSON === '') {
                    getSwaggerJson(docUri, selectedService).then((response)=>{
                        try {
                            let width = window.innerWidth - 6;
                            let height = window.innerHeight;
                            ballerinaComposer.renderAPIEditor(document.getElementById("api-visualizer"), response.ballerinaOASJson, onDidJsonChange);
                        } catch (e) {
                            console.log(e.stack);
                        }
                    })
                } else {
                    ballerinaComposer.renderAPIEditor(document.getElementById("api-visualizer"), JSON.parse(updatedJSON), onDidJsonChange);
                }
                
            }

            drawAPIEditor();
        }
    `;

    return getLibraryWebViewContent(context, body, script, styles, bodyCss, true);
}
