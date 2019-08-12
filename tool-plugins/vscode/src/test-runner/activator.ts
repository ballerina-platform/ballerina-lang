import { BallerinaExtension } from "../core";
import { commands, window } from "vscode";
import { TestRunner } from "./runner";
import { TM_EVENT_RUN_PROJECT_TESTS, CMP_TEST_RUNNER } from "../telemetry";

export function activateTestRunner(ballerinaExtInstance: BallerinaExtension) {
    const reporter = ballerinaExtInstance.telemetryReporter;
    const channel = window.createOutputChannel('Ballerina Tests');

    // register run all tests handler
    commands.registerCommand('ballerina.runAllTests', () => {
        reporter.sendTelemetryEvent(TM_EVENT_RUN_PROJECT_TESTS, { component: CMP_TEST_RUNNER });
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
                    reporter.sendTelemetryException(reason, { component: CMP_TEST_RUNNER });
                    // TODO: display error to user
                    console.log(reason.message);
                });
            }
        } 
    });
}