import { Uri } from "vscode";

export class StaticProvider {
    provideTextDocumentContent(uri: Uri) {
        return require(`.${uri.path}`);
    }
}
