import { InitializeParams, InitializeResult } from "vscode-languageserver-protocol";
import { BallerinaAST, BallerinaASTNode, BallerinaEndpoint,
    BallerinaSourceFragment } from "./ast-models";

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

export interface IBallerinaLangClient {

    init: (params?: InitializeParams) => Thenable<InitializeResult>;

    getAST: (params: GetASTParams) => Thenable<GetASTResponse>;

    astDidChange: (params: ASTDidChangeParams) => Thenable<ASTDidChangeResponse>;

    fetchExamples: (params: BallerinaExampleListParams) => Thenable<BallerinaExampleListResponse>;

    parseFragment: (params: BallerinaSourceFragment) => Thenable<BallerinaASTNode> ;

    getEndpoints: () => Thenable<BallerinaEndpoint[]>;

    getBallerinaProject: (params: GetBallerinaProjectParams) => Thenable<BallerinaProject>;

    goToSource: (line: number, column: number) => void;

    close: () => void;
}
