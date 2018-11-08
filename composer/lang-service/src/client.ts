import { BallerinaAST, BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from '@ballerina/ast-model';
import { ChildProcess } from "child_process";
// tslint:disable-next-line:no-submodule-imports
import { IConnection } from "monaco-languageclient/lib/connection";
import * as treeKill from 'tree-kill';
import { InitializeResult } from "vscode-languageserver-protocol";

export interface GetASTParams {
    documentIdentifier: {
        uri: string;
    };
}

export interface GetASTResponse {
    ast: BallerinaAST;
    parseSuccess: boolean;
}

export interface ASTDidChangeResponse {
    content?: string;
}

export interface ASTDidChangeParams {
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

export interface BallerinaExampleListParams {
    filter?: string;
}

export interface BallerinaExampleListResponse {
    samples: BallerinaExampleCategory[];
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

export class BallerinaLangClient {

    constructor(
        private childProcess: ChildProcess,
        public lsConnection: IConnection,
        public initializedResult: InitializeResult ) {
    }

    public getAST(params: GetASTParams): Thenable<GetASTResponse>{
        return this.lsConnection.sendRequest<GetASTResponse>("ballerinaDocument/ast", params);
    }

    public astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse> {
        return this.lsConnection.sendRequest("ballerinaDocument/astDidChange", params);
    }

    public fetchExamples(params: BallerinaExampleListParams = {}): Thenable<BallerinaExampleListResponse> {
        return this.lsConnection.sendRequest("ballerinaExample/list", params);
    }

    public parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode> {
        return this.lsConnection.sendRequest("ballerinaFragment/ast", params).then((resp: any)=> resp.ast);
    }

    public getEndpoints(): Thenable<BallerinaEndpoint[]> {
        return this.lsConnection.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }

    public getBallerinaProject(params: GetBallerinaProjectParams): Thenable<BallerinaProject> {
        return this.lsConnection.sendRequest("ballerinaDocument/project", params);
    }

    public kill(): void {
        this.lsConnection.shutdown();
        treeKill(this.childProcess.pid);
    }
}