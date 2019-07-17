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
import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent, getComposerWebViewOptions } from '../utils';
import { ConstructIdentifier } from 'src/core';

export function render (context: ExtensionContext, langClient: ExtendedLangClient,
    options: {currentUri: string, sourceRootUri?: string, construct?: ConstructIdentifier},
    retries: number = 1)
        : string {
   return renderDiagram(context, options);
}

function renderDiagram(context: ExtensionContext,
    constructIdentifier: {currentUri: string, sourceRootUri?: string, construct?: ConstructIdentifier}): string {
    const body = `
        <div class="ballerina-editor design-view-container" id="diagram"></div>
    `;

    const bodyCss = "diagram";

    const styles = `
        body {
            background: #f1f1f1;
        }
        .overlay {
            display: none;
        }
        .drop-zone.rect {
            fill-opacity: 0;
        }
        #diagram {
            height : 100%;
        }
        #errors {
            display: table;
            width: 100%;
            height: 100%;
        }
        #errors span { 
            display: table-cell;
            vertical-align: middle;
            text-align: center;
        }
        #warning {
            position: absolute;
            top: 15px;
            position: absolute;
            overflow: hidden;
            height: 25px;
            vertical-align: bottom;
            text-align: center;
            color: rgb(255, 90, 30);
            width: 100%;
        }
        #warning p {
            line-height: 25px;
        }
    `;

    const scripts = `
        function loadedScript() {
            let constructIdentifier = ${JSON.stringify(constructIdentifier)};
            function drawDiagram() {
                try {
                    let width = window.innerWidth - 6;
                    let height = window.innerHeight;
                    let zoom = 1;
                    const options = {
                        target: document.getElementById("diagram"),
                        editorProps: {
                            docUri: constructIdentifier.currentUri,
                            sourceRootUri: constructIdentifier.sourceRootUri,
                            initialSelectedConstruct: constructIdentifier.construct,
                            width,
                            height,
                            zoom,
                            langClient: getLangClient()
                        }
                    };
                    const diagram = ballerinaComposer.renderOverview(options);
                    webViewRPCHandler.addMethod("updateAST", (args) => {
                        diagram.updateAST();
                        return Promise.resolve({});
                    });
                    webViewRPCHandler.addMethod("selectConstruct", (args) => {
                        diagram.selectConstruct({
                            moduleName: args[0],
                            constructName: args[1],
                            subConstructName: args[2],
                        });
                        return Promise.resolve({});
                    });
                } catch(e) {
                    console.log(e.stack);
                    drawError('Oops. Something went wrong. ' + e.message);
                }
            }
            function drawError(message) {
                document.getElementById("diagram").innerHTML = \`
                <div id="errors">
                    <span>\$\{message\}</span>
                </div>
                \`;
            }
            function showWarning(message) {
                document.getElementById("warning").innerHTML = \`
                    <p><span class="fw fw-warning"></span> \$\{message\}</p>
                \`;
            }
            drawDiagram();
            enableUndoRedo();
        }
    `;

    return getLibraryWebViewContent({...getComposerWebViewOptions(), body, scripts, styles, bodyCss});
}

export function renderError() {
    return `
    <!DOCTYPE html>
    <html>
    
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
    </head>
    <body>
    <div>
        Could not connect to the parser service. Please try again after restarting vscode.
        <a href="command:workbench.action.reloadWindow">Restart</a>
    </div>
    </body>
    </html>
    `;
}
