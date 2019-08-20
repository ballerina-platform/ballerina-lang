import { ballerinaExtInstance } from "../../core";
import { commands, window } from "vscode";
import { TM_EVENT_RUN_PROJECT_BUILD, CMP_PROJECT_BUILD } from "../../telemetry";
import { runCommand, BALLERINA_COMMANDS } from "./cmd-runner";
import { getCurrentBallerinaProject } from "./utils";

export function activateBuildCommand() {
    const reporter = ballerinaExtInstance.telemetryReporter;

    // register run project build handler
    commands.registerCommand('ballerina.project.build', () => {
        reporter.sendTelemetryEvent(TM_EVENT_RUN_PROJECT_BUILD, { component: CMP_PROJECT_BUILD });
        // get Ballerina Project path for current Ballerina file
        getCurrentBallerinaProject().then((project) => {
            if (project) {
                runCommand(project, BALLERINA_COMMANDS.BUILD);
            }
        }, (reason) => {
            reporter.sendTelemetryException(reason, { component: CMP_PROJECT_BUILD });
            window.showErrorMessage(reason);
        });
    });
}