import { execSync } from "child_process";
import * as path from "path";
import * as yargs from "yargs";

// tslint:disable:no-console
const pkgScripts: any = {
    "pkg:build": "tsc --pretty",
    "pkg:build:webpack": "webpack --mode=production",
    "pkg:clean": "rimraf lib && rimraf coverage && rimraf build",
    "pkg:lint": "tslint -t stylish --project .",
    "pkg:storybook": "npx start-storybook -p 9001 -c .storybook",
    "pkg:test": "jest --colors",
    "pkg:test:coverage": "jest --coverage",
    "pkg:test:watch": "jest --watch",
    "pkg:watch": "tsc --pretty --watch",
    "pkg:watch:webpack": "webpack-dev-server --mode=development --progress"
};

process.on("unhandledRejection", (reason, promise) => {
    throw reason;
});
process.on("uncaughtException", (error) => {
    if (error) {
        console.error("Uncaught Exception: ", error.toString());
        if (error.stack) {
            console.error(error.stack);
        }
    }
});

function run(script: string): number {
    const env = process.env;
    env.TZ = "utc";
    env.FORCE_COLOR = "true";
    env.PATH = path.join(process.cwd(), "..", "..", "node_modules", ".bin")
        + path.delimiter + process.env.PATH;
    try {
        execSync(script, {
            cwd: process.cwd(),
            env,
            stdio: "inherit"
        });
        return 0;
    } catch (error) {
        return 1;
    }
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
        command,
        describe: command + " composer pkg " + pkgName,
        handler: async () => {
            let exitCode = 0;
            try {
                const args = getArgs(command);
                console.log(command + " " + pkgName);
                exitCode = run(getScript(command) + " " + args.join(" "));
            } catch (err) {
                console.error(err);
                exitCode = 1;
            }
            process.exit(exitCode);
        }
    };
}

(() => {
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
        .command(getCommand("pkg:build:webpack", pkgName))
        .command(getCommand("pkg:watch:webpack", pkgName))
        .command({
            command: "cmd:hoist",
            describe: "hoist given command",
            handler: async () => {
                let exitCode: number = 0;
                try {
                    const args = getArgs("cmd:hoist");
                    exitCode = run(args.join(" "));
                } catch (err) {
                    console.error(err);
                    exitCode = 1;
                }
                process.exit(exitCode);
            }
        });

    const commands = (yargs as any).getCommandInstance().getCommands();
    const argv = yargs.demandCommand(1).argv;
    const command = argv._[0];
    if (!command || commands.indexOf(command) === -1) {
        console.log("non-existing or no command specified");
        yargs.showHelp();
        process.exit(1);
    } else {
        yargs.help(false);
    }
})();
