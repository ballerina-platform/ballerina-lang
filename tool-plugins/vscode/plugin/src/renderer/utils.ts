import { LanguageClient } from 'vscode-languageclient';
import { PathLike } from 'fs';
import { BallerinaASTResponse } from './diagram';

export function getAST (langClient: LanguageClient, uri: PathLike): Thenable<BallerinaASTResponse> {
    const args = {
        documentIdentifier: {
            uri,
        }
    }
    return langClient.sendRequest("ballerinaDocument/ast", args);
}