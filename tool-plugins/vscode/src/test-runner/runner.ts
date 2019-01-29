import { BallerinaProject } from "src/core/extended-language-client";
import { ChildProcess, spawn } from "child_process";
import { OutputChannel } from "vscode";

export class TestRunner { 
    
    private _process : ChildProcess | undefined;

    constructor(
       private project: BallerinaProject,
       private channel: OutputChannel
    ) {
    }

    runAllTests() : void {
        this._startTestProcess(['test']);
    }

    runTestsForPackage(pkgName: string) : void {
        this._startTestProcess(['test', pkgName]);
    }

    kill() : void {
        if (this._process) {
            this._process.kill();
        }
    }

    private _startTestProcess(args: string[]) {
        this.channel.clear();
        this.channel.show();
        this._process = spawn("ballerina", args, { cwd: this.project.path });
        this._process.stdout.on('data', (data) => {
            this.channel.append(data.toString());
        });
        this._process.stderr.on('data', (data) => {
            this.channel.append(data.toString());
        });
    }
}