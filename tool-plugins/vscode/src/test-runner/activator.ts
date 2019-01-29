import { BallerinaExtension } from "../core";
import { commands, window } from "vscode";
import { TestRunner } from "./runner";

export function activateTestRunner(ballerinaExtInstance: BallerinaExtension) {
    const channel = window.createOutputChannel('Ballerina Tests');

    // register run all tests hander
    commands.registerCommand('ballerina.runAllTests', () => {
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
                }).then((project) => {
                    const runner = new TestRunner(project, channel);
                    runner.runAllTests();
                }, (reason) => {
                    // TODO: display error to user
                    console.log(reason.message);
                });
            }
        } 
    });
}