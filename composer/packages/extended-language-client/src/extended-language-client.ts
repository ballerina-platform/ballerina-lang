"use strict";
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

import { Uri } from "vscode";
import { LanguageClient } from "vscode-languageclient";

export interface BallerinaAST {
    kind: string;
    topLevelNodes: any[];
}

export interface BallerinaASTResponse {
    ast?: BallerinaAST;
}

export interface GetASTRequest {
    documentIdentifier: {
        uri: string;
    };
}

export interface ASTDidChangeResponse {
    content?: string;
}

export interface ASTDidChangeEvent {
    textDocumentIdentifier: {
        uri: string;
    };
    ast: BallerinaAST;
}

export interface BallerinaExample {
    title: string;
    url: string;
}

export interface BallerinaExampleCategory {
    title: string;
    column: number;
    samples: BallerinaExample[];
}

export interface BallerinaExampleListRequest {
    filter?: string;
}

export interface BallerinaExampleListResponse {
    samples: BallerinaExampleCategory[];
}

export interface BallerinaFragmentASTRequest {
    enclosingScope?: string;
    expectedNodeType?: string;
    source?: string;
}

// tslint:disable-next-line:no-empty-interface
export interface BallerinaFragmentASTResponse {
}

export class ExtendedLangClient extends LanguageClient {

    public getAST(uri: Uri): Thenable<BallerinaASTResponse> {
        const req: GetASTRequest = {
            documentIdentifier: {
                uri: uri.toString()
            }
        };
        return this.sendRequest("ballerinaDocument/ast", req);
    }

    public triggerASTDidChange(ast: BallerinaAST, uri: Uri): Thenable<ASTDidChangeEvent> {
        const evt: ASTDidChangeEvent = {
            ast,
            textDocumentIdentifier: {
                uri: uri.toString(),
            }
        };
        return this.sendRequest("ballerinaDocument/astDidChange", evt);
    }

    public fetchExamples(args: BallerinaExampleListRequest = {}): Thenable<BallerinaExampleListResponse> {
        return this.sendRequest("ballerinaExample/list", args);
    }

    public parseFragment(args: BallerinaFragmentASTRequest): Thenable<BallerinaFragmentASTResponse> {
        return this.sendRequest("ballerinaFragment/ast", args).then((resp: any) => resp.ast);
    }

    public getEndpoints(): Thenable<any[]> {
        return this.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }
}
