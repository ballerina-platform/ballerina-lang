// import { BallerinaExtension } from "../../core";
// import { commands, window } from "vscode";
// import { TM_EVENT_RUN_PROJECT_TESTS, CMP_TEST_RUNNER } from "../../telemetry";
// import { runCommand, BALLERINA_COMMANDS } from "./cmd-runner";

// export function activateTestRunner(ballerinaExtInstance: BallerinaExtension) {
//     const reporter = ballerinaExtInstance.telemetryReporter;

//     // register build project handler
//     commands.registerCommand('ballerina.project.build', () => {
//         reporter.sendTelemetryEvent(TM_EVENT_RUN_PROJECT_TESTS, { component: CMP_TEST_RUNNER });
//         const activeEditor = window.activeTextEditor;
//         // if currently opened file is a bal file
//         if (activeEditor && activeEditor.document.fileName.endsWith('.bal')) {
//             // get path of the current bal file
//             const uri = activeEditor.document.uri.toString();
//             if (ballerinaExtInstance.langClient) {
//                 // get Ballerina Project path for current Ballerina file
//                 ballerinaExtInstance.langClient.getBallerinaProject({
//                     documentIdentifier: {
//                         uri,
//                     }
//                 }).then((project) => {
//                     runCommand(project, BALLERINA_COMMANDS.TEST);
//                 }, (reason) => {
//                     reporter.sendTelemetryException(reason, { component: CMP_TEST_RUNNER });
//                     // TODO: display error to user
//                     console.log(reason.message);
//                 });
//             }
//         } 
//     });
// }