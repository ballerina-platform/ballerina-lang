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
    oasDefinition?: string
}

export interface BallerinaAstOasChangeResponse {
    oasAST?: string
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

    getBallerinaASTforOas(oasJson: string): Thenable<BallerinaAstOasChangeResponse> {
        const req: BallerinaAstOasChangeRequest = {
            oasDefinition: oasJson
        }
        return this.sendRequest("ballerinaDocument/astOasChange", req)
    }
}
