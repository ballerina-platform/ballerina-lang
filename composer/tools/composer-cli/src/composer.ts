import * as yargs from "yargs";
import * as path from "path";
import { exec } from "child_process";

const pkgScripts: any = {
    "pkg:watch": "tsc --pretty --watch",
    "pkg:lint": "tslint -t stylish --project .",
    "pkg:clean": "rimraf lib && rimraf coverage",
    "pkg:test": "jest",
    "pkg:build": "tsc --pretty",
    "pkg:test:watch": "jest --watch",
    "pkg:test:coverage": "jest --coverage",
    "pkg:storybook": "start-storybook -p 9001 -c .storybook"
};

process.on('unhandledRejection', (reason, promise) => {
    throw reason;
});
process.on('uncaughtException', error => {
    if (error) {
        console.error('Uncaught Exception: ', error.toString());
        if (error.stack) {
            console.error(error.stack);
        }
    }
});

function run(script: string): Promise<number> {
    return new Promise((resolve, reject) => {
        let env = process.env;
        env.PATH = process.cwd() + '/../../node_modules/.bin:' + process.env.PATH;
        const scriptProcess = exec(script, {
            'env': env,
            cwd: process.cwd()
        });
        scriptProcess.stdout.pipe(process.stdout);
        scriptProcess.stderr.pipe(process.stderr);
        scriptProcess.on('error', reject);
        scriptProcess.on('close', (code) => resolve(code));
    });
}

function getArgs(arg: string): string[] {
    const restIndex = process.argv.indexOf(arg);
    return restIndex !== -1 ? process.argv.slice(restIndex + 1) : [];
}

function getScript(name: string): string {
    return pkgScripts[name];
}

function getCommand(command: string, pkgName: string): any {
    return {
        command: command,
        describe: command + ' composer pkg ' + pkgName,
        handler: async () => {
            let exitCode: number = 0;
            try {
                const args = getArgs(command);
                console.log(command + " " + pkgName);
                exitCode = await run(getScript(command) + " " + args.join(' '));
            } catch (err) {
                console.error(err);
                exitCode = 1;
            }
            process.exit(exitCode)
        }
    }
}

(function () {
    const pkgPath = process.cwd();
    const pkgName = path.basename(pkgPath);
    yargs
        .command(getCommand("pkg:clean", pkgName))
        .command(getCommand("pkg:lint", pkgName))
        .command(getCommand("pkg:build", pkgName))
        .command(getCommand("pkg:watch", pkgName))
        .command(getCommand("pkg:test", pkgName))
        .command(getCommand("pkg:test:watch", pkgName))
        .command(getCommand("pkg:test:coverage", pkgName))
        .command(getCommand("pkg:storybook", pkgName))
        .command({
            command: 'cmd:hoist',
            describe: 'hoist given command',
            handler: async () => {
                let exitCode: number = 0;
                try {
                    const args = getArgs('cmd:hoist');
                    exitCode = await run(args.join(' '));
                } catch (err) {
                    console.error(err);
                    exitCode = 1;
                }
                process.exit(exitCode)
            }
        });

    const commands = (yargs as any).getCommandInstance().getCommands();
    const argv = yargs.demandCommand(1).argv;
    const command = argv._[0];
    if (!command || commands.indexOf(command) === -1) {
        console.log('non-existing or no command specified');
        yargs.showHelp();
        process.exit(1);
    } else {
        yargs.help(false);
    }
})();
