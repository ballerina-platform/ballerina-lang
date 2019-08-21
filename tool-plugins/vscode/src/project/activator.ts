import { BallerinaExtension, ExtendedLangClient, BALLERINA_LANG_ID, ballerinaExtInstance } from "../core";
import { workspace, window, Uri } from "vscode";
import { TM_EVENT_OPEN_DETECTED_PROJECT_ROOT_VIA_PROMPT, CMP_PROJECT_SUPPORT } from "../telemetry";

function promptOpenFolder(path: string) {
    const reporter = ballerinaExtInstance.telemetryReporter;
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
                reporter.sendTelemetryEvent(TM_EVENT_OPEN_DETECTED_PROJECT_ROOT_VIA_PROMPT, { component: CMP_PROJECT_SUPPORT });
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