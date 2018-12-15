import { GetASTParams, GetASTResponse, ASTDidChangeParams, ASTDidChangeResponse,
        BallerinaExampleListParams, BallerinaExampleListResponse, GetBallerinaProjectParams,
        BallerinaProject, GoToSourceParams, RevealRangeParams, IBallerinaLangClient 
    } from "@ballerina/lang-service/lib/src/client/model";
import { InitializeParams, InitializeResult,  } from "@ballerina/lang-service/node_modules/vscode-languageserver-protocol/lib/protocol";
import { 
        BallerinaSourceFragment, BallerinaASTNode, BallerinaEndpoint 
    } from "@ballerina/lang-service/lib/src/client/ast-models";
import { ILanguageClient } from "@theia/languages/lib/browser";


export class BallerinaLangClient implements IBallerinaLangClient {

    isInitialized: boolean = true;

    constructor(private langClient: ILanguageClient) {
    }

    public init(params?: InitializeParams): Thenable<InitializeResult> {
        return Promise.resolve(this.langClient.initializeResult as InitializeResult);
    }

    public getAST(params: GetASTParams): Thenable<GetASTResponse> {
        return this.langClient.sendRequest<GetASTResponse>("ballerinaDocument/ast", params);
    }

    public astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse> {
        return this.langClient.sendRequest("ballerinaDocument/astDidChange", params);
    }

    public fetchExamples(params: BallerinaExampleListParams = {}): Thenable<BallerinaExampleListResponse> {
        return this.langClient.sendRequest("ballerinaExample/list", params);
    }

    public parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode> {
        return this.langClient.sendRequest("ballerinaFragment/ast", params).then((resp: any) => resp.ast);
    }

    public getEndpoints(): Thenable<BallerinaEndpoint[]> {
        return this.langClient.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }

    public getBallerinaProject(params: GetBallerinaProjectParams): Thenable<BallerinaProject> {
        return this.langClient.sendRequest("ballerinaDocument/project", params);
    }

    public goToSource(params: GoToSourceParams): void {
       // TODO
    }

    public revealRange(params: RevealRangeParams): void {
        // TODO
    }

    public close(): void {
        // TODO
    }
}