import { BallerinaProject, ballerinaExtInstance } from "../../core";
import { window } from "vscode";

export function getCurrentBallerinaProject() : Promise<BallerinaProject> {
    return new Promise((resolve, reject) => {
        const activeEditor = window.activeTextEditor;
        // if currently opened file is a bal file
        if (activeEditor && activeEditor.document.fileName.endsWith('.bal')) {
            // get path of the current bal file
            const uri = activeEditor.document.uri.toString();
            if (ballerinaExtInstance.langClient) {
                // get Ballerina Project path for current Ballerina file
                ballerinaExtInstance.langClient.getBallerinaProject({
                    documentIdentifier: {
                        uri,
                    }
                }).then(resolve, reject);
            } else {
                reject("Language Client is not available.");
            }
        } else {
            reject("Current file is not a Ballerina file");
        }
    });
}