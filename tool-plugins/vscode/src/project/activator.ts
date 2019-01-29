import { BallerinaExtension, ExtendedLangClient, BALLERINA_LANG_ID } from "../core";
import { workspace, window, Uri } from "vscode";

function promptOpenFolder(path: string) {
    if (workspace.workspaceFolders) {
        const folder = workspace.workspaceFolders.find((folder) => {
            return folder.uri.fsPath === path;
        });
        if (folder) {
            return;
        }
    }
    const action = "Open Project";
    window.showInformationMessage("File resides within a Ballerina project at " +
        path, action)
        .then((selection) => {
            if (selection === action) {
                workspace.updateWorkspaceFolders(0, 0, { uri: Uri.file(path)});
            }
        });
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
    let langClient = <ExtendedLangClient>ballerinaExtInstance.langClient;
    // when a new file is opened, detect if it resides inside a project
    // and notify user
    workspace.onDidOpenTextDocument((document) => {
        if (document.languageId === BALLERINA_LANG_ID) {
            langClient.getBallerinaProject({
                documentIdentifier: {
                    uri: document.uri.toString()
                }
            }).then((project) => {
                if (project.path) {
                    promptOpenFolder(project.path);
                }
            });
        }
    });
}