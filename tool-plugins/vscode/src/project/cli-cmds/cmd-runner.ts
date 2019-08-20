import { BallerinaProject } from "../../core/extended-language-client";
import { getCLIOutputChannel } from "./output";
import { spawn } from "child_process";

export enum BALLERINA_COMMANDS {
    TEST = "test", BUILD = "build", FORMAT = "format"
}

export function runCommand(project: BallerinaProject, cmd: BALLERINA_COMMANDS, ...args: string[]) {
    const outputChannel = getCLIOutputChannel();
    outputChannel.clear();
    outputChannel.show();
    outputChannel.appendLine("executing command: ballerina " + cmd + " at " + project.path + "\n");
    const process = spawn("ballerina", [cmd, ...args], { cwd: project.path });
    process.stdout.on('data', (data) => {
        outputChannel.append(data.toString());
    });
    process.stderr.on('data', (data) => {
        outputChannel.append(data.toString());
    });
    process.on("exit", () => {
        outputChannel.appendLine("Finished ballerina " + cmd + " command execution.");
    });
}