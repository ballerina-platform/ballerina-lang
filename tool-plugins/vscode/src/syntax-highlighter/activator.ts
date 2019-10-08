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
import { BallerinaExtension } from '../core';
import { commands, ExtensionContext, Uri } from 'vscode';
import { ExtendedLangClient } from '../core';
//import * as vscode from 'vscode';

export function activate(ballerinaExtInstance: BallerinaExtension) {

    const context = <ExtensionContext> ballerinaExtInstance.context;

    let disposable = commands.registerCommand('ballerina.highlightSyntax', () => {
        const langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;
        const docUri = Uri.file("/home/tharushi/ballerina-lang/tool-plugins/vscode/abc.bal");
        console.log(docUri);
        
        langClient.getAST(docUri)
					.then((resp) => {
						if (resp.ast) {
							console.log("Printing AST-");
                            console.log(resp.ast);
                        }
                    });
	});
	context.subscriptions.push(disposable);

}
