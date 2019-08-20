import { ballerinaExtInstance } from "../../core";
import { commands, window } from "vscode";
import { TM_EVENT_RUN_PROJECT_TESTS, CMP_PROJECT_TEST_RUNNER } from "../../telemetry";
import { runCommand, BALLERINA_COMMANDS } from "./cmd-runner";
import { getCurrentBallerinaProject } from "./utils";

export function activateTestRunner() {
    const reporter = ballerinaExtInstance.telemetryReporter;

    // register run project tests handler
    commands.registerCommand('ballerina.project.test', () => {
        reporter.sendTelemetryEvent(TM_EVENT_RUN_PROJECT_TESTS, { component: CMP_PROJECT_TEST_RUNNER });
        // get Ballerina Project path for current Ballerina file
        getCurrentBallerinaProject().then((project) => {
            if (project) {
                runCommand(project, BALLERINA_COMMANDS.TEST);
            }
        }, (reason) => {
            reporter.sendTelemetryException(reason, { component: CMP_PROJECT_TEST_RUNNER });
            window.showErrorMessage(reason);
        });
    });
}