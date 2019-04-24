// tslint:disable-next-line:no-submodule-imports
import { IConnection } from "monaco-languageclient/lib/connection";
import { InitializeParams, InitializeResult,
    Location, TextDocumentPositionParams } from "vscode-languageserver-protocol";
import { BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from "./ast-models";
import { ASTDidChangeParams, ASTDidChangeResponse, BallerinaExampleListParams,
    BallerinaExampleListResponse, BallerinaProject, GetASTParams, GetASTResponse,
    GetBallerinaProjectParams, GoToSourceParams, IBallerinaLangClient, RevealRangeParams } from "./model";

export class BallerinaLangClient implements IBallerinaLangClient {

    public isInitialized: boolean = false;

    constructor(
        public lsConnection: IConnection) {
    }

    public init(params: InitializeParams = initParams): Thenable<InitializeResult> {
        this.lsConnection.listen();
        return this.lsConnection.initialize(params)
                .then((resp) => {
                    this.isInitialized = true;
                    return resp;
                });
    }

    public getAST(params: GetASTParams): Thenable<GetASTResponse> {
        return this.lsConnection.sendRequest<GetASTResponse>("ballerinaDocument/ast", params);
    }

    public astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse> {
        return this.lsConnection.sendRequest("ballerinaDocument/astDidChange", params);
    }

    public fetchExamples(params: BallerinaExampleListParams = {}): Thenable<BallerinaExampleListResponse> {
        return this.lsConnection.sendRequest("ballerinaExample/list", params);
    }

    public parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode> {
        return this.lsConnection.sendRequest("ballerinaFragment/ast", params).then((resp: any) => resp.ast);
    }

    public getEndpoints(): Thenable<BallerinaEndpoint[]> {
        return this.lsConnection.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }

    public getBallerinaProject(params: GetBallerinaProjectParams): Thenable<BallerinaProject> {
        return this.lsConnection.sendRequest("ballerinaDocument/project", params);
    }

    public getDefinitionPosition(params: TextDocumentPositionParams): Thenable<Location> {
        // TODO
        return Promise.reject("Not implemented");
    }

    public goToSource(params: GoToSourceParams): void {
       // TODO
    }

    public revealRange(params: RevealRangeParams): void {
        // TODO
    }

    public close(): void {
        this.lsConnection.shutdown();
    }
}

export const initParams: InitializeParams = {
    capabilities: {
    },
    processId: process.pid,
    rootUri: null,
    workspaceFolders: null,
};
