import { InitializeParams, InitializeResult,
    Location, TextDocumentPositionParams } from "vscode-languageserver-protocol";
import { BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from "./ast-models";
import { ASTDidChangeParams, ASTDidChangeResponse, BallerinaExampleListParams,
    BallerinaExampleListResponse, BallerinaProject, GetASTParams, GetASTResponse, GetBallerinaProjectParams,
    GetProjectASTParams, GetProjectASTResponse, GoToSourceParams, IBallerinaLangClient,
    RevealRangeParams } from "./model";

// tslint:disable:no-object-literal-type-assertion
export class EmptyLanguageClient implements IBallerinaLangClient {

    public isInitialized: boolean = false;

    public init(params?: InitializeParams): Thenable<InitializeResult> {
        return Promise.reject();
    }

    public getProjectAST(params: GetProjectASTParams): Thenable<GetProjectASTResponse> {
        return Promise.reject();
    }

    public getAST(params: GetASTParams): Thenable<GetASTResponse> {
        return Promise.reject();
    }

    public astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse> {
        return Promise.reject();
    }

    public fetchExamples(params: BallerinaExampleListParams = {}): Thenable<BallerinaExampleListResponse> {
        return Promise.reject();
    }

    public parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode> {
        return Promise.reject();
    }

    public getEndpoints(): Thenable<BallerinaEndpoint[]> {
        return Promise.reject();
    }

    public getBallerinaProject(params: GetBallerinaProjectParams): Thenable<BallerinaProject> {
        return Promise.reject();
    }

    public getDefinitionPosition(params: TextDocumentPositionParams): Thenable<Location> {
        return Promise.reject();
    }

    public goToSource(params: GoToSourceParams): void {
        // EMPTY
    }

    public revealRange(params: RevealRangeParams): void {
        // EMPTY
    }

    public close(): void {
        // EMPTY
    }
}
