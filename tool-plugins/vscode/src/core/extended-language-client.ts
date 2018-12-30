'use strict';
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

import { LanguageClient } from "vscode-languageclient";
import { Uri } from "vscode";

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
    samples: Array<BallerinaExample>;
}   

export interface BallerinaExampleListRequest {
    filter?: string;
}

export interface BallerinaExampleListResponse {
    samples: Array<BallerinaExampleCategory>;
}

export interface BallerinaFragmentASTRequest {
    enclosingScope?: string;
    expectedNodeType?: string;
    source?: string;
}

export interface BallerinaFragmentASTResponse {
}

export interface BallerinaOASResponse {
    ballerinaOASJson?: string;
}

export interface BallerinaOASRequest {
    ballerinaDocument: {
        uri: string;
    };
    ballerinaService?: string;
}

export interface BallerinaAstOasChangeRequest {
    oasDefinition?: string,
    documentIdentifier: {
        uri: string;
    };
}

export interface BallerinaProject {
    path?: string;
    version?: string;
    author?: string;
}

export interface GetBallerinaProjectParams {
    documentIdentifier: {
        uri: string;
    };
}

export interface BallerinaAstOasChangeResponse {
    oasAST?: string
}

export interface BallerinaServiceListRequest {
    documentIdentifier: {
        uri: string;
    };
}

export interface BallerinaServiceListResponse {
    services: string[];
}

export class ExtendedLangClient extends LanguageClient {

    getAST(uri: Uri): Thenable<BallerinaASTResponse> {
        const req: GetASTRequest = {
            documentIdentifier: {
                uri: uri.toString()
            }
        };
        return this.sendRequest("ballerinaDocument/ast", req);
    }

    triggerASTDidChange(ast: BallerinaAST, uri: Uri): Thenable<ASTDidChangeEvent> {
        const evt: ASTDidChangeEvent = {
            textDocumentIdentifier: {
                uri: uri.toString(),
            },
            ast
        };
        return this.sendRequest("ballerinaDocument/astDidChange", evt);
    }

    fetchExamples(args: BallerinaExampleListRequest = {}): Thenable<BallerinaExampleListResponse> {
        return this.sendRequest("ballerinaExample/list", args);
    }

    parseFragment(args: BallerinaFragmentASTRequest): Thenable<BallerinaFragmentASTResponse> {
        return this.sendRequest("ballerinaFragment/ast", args).then((resp: any)=> resp.ast);
    }

    getEndpoints(): Thenable<Array<any>> {
        return this.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }

    getBallerinaOASDef(uri: Uri, oasService: string): Thenable<BallerinaOASResponse> {
        const req: BallerinaOASRequest = {
            ballerinaDocument: {
                uri: uri.toString()
            },
            ballerinaService: oasService
        }
        return this.sendRequest("ballerinaDocument/swaggerDef", req);
    }

    triggerSwaggerDefChange(oasJson: string, uri: Uri): void {
        const req: BallerinaAstOasChangeRequest = {
            oasDefinition: oasJson,
            documentIdentifier: {
                uri: uri.toString()
            },
        }
        return this.sendNotification("ballerinaDocument/apiDesignDidChange", req);
    }

    getServiceListForActiveFile(uri: Uri): Thenable<BallerinaServiceListResponse> {
        const req: BallerinaServiceListRequest = {
            documentIdentifier: {
                uri: uri.toString()
            },
        }
        return this.sendRequest("ballerinaDocument/serviceList", req)
    }

    getBallerinaProject(params: GetBallerinaProjectParams): Thenable<BallerinaProject> {
        return this.sendRequest("ballerinaDocument/project", params);
    }
}
