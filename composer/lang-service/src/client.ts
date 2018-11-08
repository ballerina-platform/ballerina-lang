import { BallerinaAST } from '@ballerina/ast-model';
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

export class BallerinaLangClient {

    constructor(
        private childProcess: ChildProcess,
        public lsConnection: IConnection,
        public initializedResult: InitializeResult ) {
    }

    public getAST(params: GetASTParams): Thenable<GetASTResponse>{
        return this.lsConnection.sendRequest<GetASTResponse>("ballerinaDocument/ast", params);
    }

    public kill(): void {
        this.lsConnection.shutdown();
        treeKill(this.childProcess.pid);
    }
}