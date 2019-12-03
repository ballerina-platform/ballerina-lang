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
 */

import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent, WebViewOptions, getComposerWebViewOptions } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient) 
    : string {
    const scripts = `
        function loadedScript() {
            window.addEventListener('message', event => {
                const message = event.data; // The JSON data our extension sent
                switch (message.command) {
                    case 'updateTraces':
                        drawTraces();
                        break;
                }
            });
            function drawTraces() {
                try {
                    webViewRPCHandler.invokeRemoteMethod('getTraces', [], (traces) => {
                        console.log(traces, window);
                        ballerinaComposer.renderTraceLogs(document.getElementById("traces"), traces, (trace)=>{
                            webViewRPCHandler.invokeRemoteMethod('showDetails', trace);
                        }, ()=>{
                            console.log('clear logs');
                            webViewRPCHandler.invokeRemoteMethod('clearLogs');
                        });
                    });
                    console.log('Successfully rendered tracing...');
                } catch(e) {
                    console.log(e.stack);
                    drawError('Oops. Something went wrong.');
                }
            }
            
            drawTraces();
        }
            `;
    const body = `<div id="traces"> <br/> 
            <div class="ui active transition visible dimmer" style="display: flex !important;">
                <div class="content">
                    <div class="ui text loader">Loading</div>
                </div>
            </div>
    </div>`;

    const bodyCss = "network-logs";

    const styles = `
        body.network-logs{
            overflow: auto!important;
        }
        body.vscode-dark, body.vscode-light {
            background-color: #1e1e1e;
            color: white;
        }
        .clickable {
            cursor: pointer;
        }
        .inverted .ui.dropdown .menu .item .text {
            color: #1e1e1e!important;
        }
        .ui.menu {
            background-color: #1e1e1e!important;
        }
        .icon.inbound {
            color: #00bcd4;
        }
        .ui.label{
            background: #cecece!important;
        }
        .icon.outbound {
            color: #f1772a;
        }
        .ui.table tr.active.clickable, .ui.table td.active.clickable{
            background: #565656 !important;
            color: rgba(255, 255, 255, 0.9)!important;
        }
        table td, table th{
            max-width: 100px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;        
            padding: 0.5em!important;
        }
        table tr td.direction-icon{
            width: 25px;
            text-align: center;
        }
        table tr td.method, table tr th.method{
            width: 100px;
            text-align: center!important;
        }
        .ui.inverted.segment{
            background: #1e1e1e!important;
        }
        .ui.active.transition.visible.dimmer{
            background: #1e1e1e!important;
        }
        .ui.horizontal.label {
            color: #ffffff;
            -webkit-text-stroke: .025em rgba(0,0,0,0.2);
            margin: 0!important;
        }
        .ui.horizontal.label.post {background-color: #49cc90!important; }
        .ui.horizontal.label.put {background-color: #fca130!important; }
        .ui.horizontal.label.get {background-color: #61affe!important; }
        .ui.horizontal.label.delete {background-color: #f93e3e!important; }
        .ui.horizontal.label.patch {background-color: #50e3c2!important; }
        .ui.horizontal.label.head {background-color: #9012fe!important; }
        .ui.horizontal.label.options {background-color: #0d5aa7!important; }
        #logs-console table thead{
            display: table-header-group!important;
        }
    `;
    const webViewOptions: WebViewOptions = {
        ...getComposerWebViewOptions(),
        body, scripts, styles, bodyCss
    };
    
    return getLibraryWebViewContent(webViewOptions);
}


export function renderDetailView (context: ExtensionContext, langClient: ExtendedLangClient, trace: any) {
    const body = `<div id="trace-details"><br/> 
            <div class="ui active transition visible dimmer" style="display: flex !important;">
                <div class="content">
                    <div class="ui text loader">Loading</div>
                </div>
            </div>
    </div>`;

    const bodyCss = "network-logs network-logs-details";

    const traceString = encodeURIComponent(JSON.stringify(trace));

    const styles = `
        body.network-logs{
            overflow: auto!important;
        }
        body.vscode-dark, body.vscode-light {
            background-color: #1e1e1e;
            color: white;
        }
        #trace-details code {
            border: none;
        }
        #trace-details {
            color: #ffffe6;
        }
        #trace-details strong{
            font-weight: 600;
        }
        .ui.inverted.segment{
            background: #1e1e1e!important;
        }
        code {
            background: none!important;
            padding: 0!important;
            color: #d7ba7d!important;
        }
        pre {
            margin: 0!important;
        }
        .ui.active.transition.visible.dimmer{
            background: #1e1e1e!important;
        }
        .payload{
            margin-top:15px;
        }
        `;

    const scripts = `
        function loadedScript() {
            function renderDetailedTrace(trace) {
                try {
                    ballerinaComposer.renderDetailedTrace(document.getElementById("trace-details"), trace);
                    console.log('Successfully rendered trace details...');
                } catch(e) {
                    console.log(e.stack);
                    drawError('Oops. Something went wrong.');
                }
            }
            renderDetailedTrace("${traceString}");
        }
    `;

    const webViewOptions: WebViewOptions = {
        ...getComposerWebViewOptions(),
        body, scripts, styles, bodyCss
    };
    
    return getLibraryWebViewContent(webViewOptions);
}
