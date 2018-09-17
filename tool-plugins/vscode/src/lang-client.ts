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

    getEndpoints(): Thenable<Array<any>> {
        return this.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }
}
